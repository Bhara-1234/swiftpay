package com.swiftpay.ledgerservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swiftpay.ledgerservice.dto.TransactionHistoryResponse;
import com.swiftpay.ledgerservice.entity.Transaction;
import com.swiftpay.ledgerservice.entity.UserAccount;
import com.swiftpay.ledgerservice.enums.TransactionStatus;
import com.swiftpay.ledgerservice.event.PaymentEvent;
import com.swiftpay.ledgerservice.event.PaymentStatusEvent;
import com.swiftpay.ledgerservice.exception.ResourceNotFoundException;
import com.swiftpay.ledgerservice.repository.TransactionRepository;
import com.swiftpay.ledgerservice.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

	private final UserAccountRepository userRepository;

	private final TransactionRepository transactionRepository;

	private final KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate;

	@Transactional
	public void processPayment(PaymentEvent event) {

		log.info("Processing Payment : {}", event.getTransactionId());

		Transaction transaction = transactionRepository.findByTransactionId(event.getTransactionId())
				.orElseThrow(() -> new ResourceNotFoundException("Transaction Not Found"));

		UserAccount sender = userRepository.findById(event.getSenderId())
				.orElseThrow(() -> new ResourceNotFoundException("Sender Account Not Found"));

		UserAccount receiver = userRepository.findById(event.getReceiverId())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver Account Not Found"));

		BigDecimal amount = event.getAmount();

		if (sender.getBalance().compareTo(amount) < 0) {

			transaction.setStatus(TransactionStatus.FAILED);

			transactionRepository.save(transaction);

			kafkaTemplate.send("payment-failed",
					new PaymentStatusEvent(event.getTransactionId(), "FAILED", "Insufficient Balance"));

			log.error("Payment Failed : Insufficient balance for txn {}", event.getTransactionId());

			return;
		}

		sender.setBalance(sender.getBalance().subtract(amount));

		receiver.setBalance(receiver.getBalance().add(amount));

		userRepository.save(sender);
		userRepository.save(receiver);

		transaction.setStatus(TransactionStatus.SUCCESS);

		transactionRepository.save(transaction);

		kafkaTemplate.send("payment-completed",
				new PaymentStatusEvent(event.getTransactionId(), "SUCCESS", "Payment Completed Successfully"));

		log.info("Payment {} completed successfully", event.getTransactionId());
	}

	public List<TransactionHistoryResponse> getTransactions(Long userId) {

		return transactionRepository.findBySenderIdOrReceiverId(userId, userId).stream()
				.map(txn -> new TransactionHistoryResponse(txn.getTransactionId(), txn.getSenderId(),
						txn.getReceiverId(), txn.getAmount(), txn.getCurrency(), txn.getStatus().name()))
				.toList();
	}

	public UserAccount createUser(UserAccount user) {

		boolean exists = userRepository.existsById(user.getId());

		if (exists) {
			throw new RuntimeException("User already exists");
		}

		return userRepository.save(user);
	}
}