package com.swiftpay.gatewayservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.swiftpay.gatewayservice.dto.PaymentRequest;
import com.swiftpay.gatewayservice.dto.PaymentResponse;
import com.swiftpay.gatewayservice.entity.Transaction;
import com.swiftpay.gatewayservice.entity.UserAccount;
import com.swiftpay.gatewayservice.enums.TransactionStatus;
import com.swiftpay.gatewayservice.event.PaymentEvent;
import com.swiftpay.gatewayservice.exception.ResourceNotFoundException;
import com.swiftpay.gatewayservice.exception.UserAlreadyExistsException;
import com.swiftpay.gatewayservice.repository.TransactionRepository;
import com.swiftpay.gatewayservice.repository.UserAccountRepository;
import com.swiftpay.gatewayservice.service.PaymentService;
import com.swiftpay.gatewayservice.util.PaymentValidationUtil;

@ExtendWith(MockitoExtension.class)
class PaymentServiceApplicationTests {

	@Mock
	private TransactionRepository repository;

	@Mock
	private UserAccountRepository userRepository;

	@Mock
	private PaymentValidationUtil validationUtil;

	@Mock
	private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

	@InjectMocks
	private PaymentService paymentService;

	@Test
	void processPayment_Success() {

		PaymentRequest request = new PaymentRequest("TXN001", 1L, 2L, new BigDecimal("1000"), "INR");

		doNothing().when(validationUtil).validatePaymentRequest(any(PaymentRequest.class));

		PaymentResponse response = paymentService.processPayment(request);

		assertNotNull(response);
		assertEquals("TXN001", response.getTransactionId());
		assertEquals("PENDING", response.getStatus());
		assertEquals("Payment accepted for processing", response.getMessage());

		verify(validationUtil, times(1)).validatePaymentRequest(request);

		verify(repository, times(1)).save(any(Transaction.class));

		verify(kafkaTemplate, times(1)).send(eq("payment-initiated"), any(PaymentEvent.class));
	}

	@Test
	void getPaymentStatus_Success() {

		Transaction txn = new Transaction();
		txn.setTransactionId("TXN001");
		txn.setStatus(TransactionStatus.SUCCESS);

		when(repository.findByTransactionId("TXN001")).thenReturn(Optional.of(txn));

		PaymentResponse response = paymentService.getPaymentStatus("TXN001");

		assertNotNull(response);
		assertEquals("TXN001", response.getTransactionId());
		assertEquals("SUCCESS", response.getStatus());
		assertEquals("Transaction status fetched successfully", response.getMessage());

		verify(repository, times(1)).findByTransactionId("TXN001");
	}

	@Test
	void getPaymentStatus_TransactionNotFound() {

		when(repository.findByTransactionId("TXN001")).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> paymentService.getPaymentStatus("TXN001"));

		assertEquals("Transaction Not Found", exception.getMessage());

		verify(repository, times(1)).findByTransactionId("TXN001");
	}

	@Test
	void createUser_Success() {

		UserAccount user = new UserAccount();
		user.setId(1L);
		user.setBalance(new BigDecimal("5000"));

		when(userRepository.existsById(1L)).thenReturn(false);

		when(userRepository.save(user)).thenReturn(user);

		UserAccount savedUser = paymentService.createUser(user);

		assertNotNull(savedUser);
		assertEquals(1L, savedUser.getId());
		assertEquals(new BigDecimal("5000"), savedUser.getBalance());

		verify(userRepository, times(1)).existsById(1L);

		verify(userRepository, times(1)).save(user);
	}

	@Test
	void createUser_UserAlreadyExists() {

		UserAccount user = new UserAccount();
		user.setId(1L);

		when(userRepository.existsById(1L)).thenReturn(true);

		UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
				() -> paymentService.createUser(user));

		assertEquals("User already exists", exception.getMessage());

		verify(userRepository, times(1)).existsById(1L);

		verify(userRepository, never()).save(any(UserAccount.class));
	}
}