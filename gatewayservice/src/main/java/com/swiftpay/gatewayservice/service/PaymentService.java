package com.swiftpay.gatewayservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
import com.swiftpay.gatewayservice.util.PaymentValidationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final TransactionRepository repository;

	private final UserAccountRepository userRepository;

	private final PaymentValidationUtil validationUtil;

	private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

	public PaymentResponse processPayment(PaymentRequest request) {

		log.info("Processing payment request for transaction: {}", request.getTransactionId());

		log.debug("Payment request payload: {}", request);

		validationUtil.validatePaymentRequest(request);

		log.info("Validation successful for transaction: {}", request.getTransactionId());

		Transaction transaction = new Transaction();

		transaction.setTransactionId(request.getTransactionId());

		transaction.setSenderId(request.getSenderId());

		transaction.setReceiverId(request.getReceiverId());

		transaction.setAmount(request.getAmount());

		transaction.setCurrency(request.getCurrency().toUpperCase());

		transaction.setStatus(TransactionStatus.PENDING);

		repository.save(transaction);

		log.info("Transaction {} saved with status PENDING", request.getTransactionId());

		PaymentEvent event = new PaymentEvent(request.getTransactionId(), request.getSenderId(),
				request.getReceiverId(), request.getAmount(), request.getCurrency());

		log.debug("Publishing Kafka event: {}", event);

		kafkaTemplate.send("payment-initiated", event);

		log.info("Payment event published to Kafka for transaction: {}", request.getTransactionId());

		return new PaymentResponse(request.getTransactionId(), "PENDING", "Payment accepted for processing");
	}

	public PaymentResponse getPaymentStatus(String transactionId) {

		log.info("Fetching payment status for transaction: {}", transactionId);

		Transaction txn = repository.findByTransactionId(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction Not Found"));

		log.info("Transaction {} status is {}", transactionId, txn.getStatus());

		return new PaymentResponse(txn.getTransactionId(), txn.getStatus().name(), txn.getRemarks());
	}

	public UserAccount createUser(UserAccount user) {

		log.info("Creating user with id: {}", user.getId());

		log.debug("User payload: {}", user);

		boolean exists = userRepository.existsById(user.getId());

		if (exists) {

			log.error("User already exists with id: {}", user.getId());

			throw new UserAlreadyExistsException("User already exists");
		}

		UserAccount savedUser = userRepository.save(user);

		log.info("User created successfully with id: {}", savedUser.getId());

		return savedUser;
	}
}