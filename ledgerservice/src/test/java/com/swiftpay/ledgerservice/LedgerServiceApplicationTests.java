package com.swiftpay.ledgerservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.swiftpay.ledgerservice.dto.TransactionHistoryResponse;
import com.swiftpay.ledgerservice.entity.Transaction;
import com.swiftpay.ledgerservice.entity.UserAccount;
import com.swiftpay.ledgerservice.enums.TransactionStatus;
import com.swiftpay.ledgerservice.event.PaymentEvent;
import com.swiftpay.ledgerservice.event.PaymentStatusEvent;
import com.swiftpay.ledgerservice.repository.TransactionRepository;
import com.swiftpay.ledgerservice.repository.UserAccountRepository;
import com.swiftpay.ledgerservice.service.LedgerService;
import com.swiftpay.ledgerservice.util.LedgerValidationUtil;

@ExtendWith(MockitoExtension.class)
class LedgerServiceApplicationTests {

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerValidationUtil validationUtil;

    @Mock
    private KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void processPayment_Success() {

        PaymentEvent event = new PaymentEvent(
                "TXN001",
                1L,
                2L,
                new BigDecimal("100"),
                "INR");

        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN001");
        transaction.setStatus(TransactionStatus.PENDING);

        UserAccount sender = new UserAccount();
        sender.setId(1L);
        sender.setBalance(new BigDecimal("1000"));

        UserAccount receiver = new UserAccount();
        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("500"));

        when(validationUtil.validateTransaction(event))
                .thenReturn(transaction);

        when(validationUtil.validateSender(event))
                .thenReturn(sender);

        when(validationUtil.validateReceiver(event))
                .thenReturn(receiver);

        when(validationUtil.validateBalance(sender,
                new BigDecimal("100")))
                .thenReturn(true);

        ledgerService.processPayment(event);

        assertEquals(new BigDecimal("900"),
                sender.getBalance());

        assertEquals(new BigDecimal("600"),
                receiver.getBalance());

        assertEquals(TransactionStatus.SUCCESS,
                transaction.getStatus());

        verify(userRepository, times(1)).save(sender);
        verify(userRepository, times(1)).save(receiver);

        verify(transactionRepository, times(1))
                .save(transaction);

        verify(kafkaTemplate, times(1))
                .send(eq("payment-completed"),
                        any(PaymentStatusEvent.class));
    }

    @Test
    void processPayment_InsufficientBalance() {

        PaymentEvent event = new PaymentEvent(
                "TXN002",
                1L,
                2L,
                new BigDecimal("1000"),
                "INR");

        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN002");

        UserAccount sender = new UserAccount();
        sender.setId(1L);
        sender.setBalance(new BigDecimal("500"));

        UserAccount receiver = new UserAccount();
        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("100"));

        when(validationUtil.validateTransaction(event))
                .thenReturn(transaction);

        when(validationUtil.validateSender(event))
                .thenReturn(sender);

        when(validationUtil.validateReceiver(event))
                .thenReturn(receiver);

        when(validationUtil.validateBalance(sender,
                new BigDecimal("1000")))
                .thenReturn(false);

        ledgerService.processPayment(event);

        assertEquals(TransactionStatus.FAILED,
                transaction.getStatus());

        verify(transactionRepository, times(1))
                .save(transaction);

        verify(userRepository, never())
                .save(sender);

        verify(userRepository, never())
                .save(receiver);

        verify(kafkaTemplate, times(1))
                .send(eq("payment-failed"),
                        any(PaymentStatusEvent.class));
    }

    @Test
    void getTransactions_Success() {

        Transaction transaction = new Transaction();

        transaction.setTransactionId("TXN001");
        transaction.setSenderId(1L);
        transaction.setReceiverId(2L);
        transaction.setAmount(new BigDecimal("100"));
        transaction.setCurrency("INR");
        transaction.setStatus(TransactionStatus.SUCCESS);

        when(transactionRepository
                .findBySenderIdOrReceiverId(1L, 1L))
                .thenReturn(List.of(transaction));

        List<TransactionHistoryResponse> response =
                ledgerService.getTransactions(1L);

        assertNotNull(response);
        assertEquals(1, response.size());

        TransactionHistoryResponse txn = response.get(0);

        assertEquals("TXN001",
                txn.getTransactionId());

        assertEquals(1L,
                txn.getSenderId());

        assertEquals(2L,
                txn.getReceiverId());

        assertEquals(new BigDecimal("100"),
                txn.getAmount());

        assertEquals("INR",
                txn.getCurrency());

        assertEquals("SUCCESS",
                txn.getStatus());

        verify(transactionRepository, times(1))
                .findBySenderIdOrReceiverId(1L, 1L);
    }

    @Test
    void getTransactions_EmptyList() {

        when(transactionRepository
                .findBySenderIdOrReceiverId(1L, 1L))
                .thenReturn(List.of());

        List<TransactionHistoryResponse> response =
                ledgerService.getTransactions(1L);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(transactionRepository, times(1))
                .findBySenderIdOrReceiverId(1L, 1L);
    }
}