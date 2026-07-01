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
import com.swiftpay.ledgerservice.repository.TransactionRepository;
import com.swiftpay.ledgerservice.repository.UserAccountRepository;
import com.swiftpay.ledgerservice.util.LedgerValidationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

	private final UserAccountRepository userRepository;

	private final TransactionRepository transactionRepository;

	private final LedgerValidationUtil validationUtil;

	private final KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate;

	@Transactional
	public void processPayment(PaymentEvent event) {

		log.info("Processing payment for transaction : {}", event.getTransactionId());

		log.debug("Received payment event : {}", event);

		Transaction transaction = validationUtil.validateTransaction(event);

		UserAccount sender = validationUtil.validateSender(event);

		UserAccount receiver = validationUtil.validateReceiver(event);

		log.debug("Sender details : {}", sender);

		log.debug("Receiver details : {}", receiver);

		BigDecimal amount = event.getAmount();

		log.debug("Amount to transfer : {}", amount);

		if (!validationUtil.validateBalance(sender, amount)) {

			transaction.setStatus(TransactionStatus.FAILED);

			transactionRepository.save(transaction);

			kafkaTemplate.send("payment-failed",
					new PaymentStatusEvent(event.getTransactionId(), "FAILED", "Insufficient Balance"));

			log.error("Payment failed due to insufficient balance for transaction : {}", event.getTransactionId());

			return;
		}

		sender.setBalance(sender.getBalance().subtract(amount));

		receiver.setBalance(receiver.getBalance().add(amount));

		log.debug("Updated sender balance : {}", sender.getBalance());

		log.debug("Updated receiver balance : {}", receiver.getBalance());

		userRepository.save(sender);

		userRepository.save(receiver);

		transaction.setStatus(TransactionStatus.SUCCESS);

		transactionRepository.save(transaction);

		log.info("Transaction {} status updated to SUCCESS", event.getTransactionId());

		kafkaTemplate.send("payment-completed",
				new PaymentStatusEvent(event.getTransactionId(), "SUCCESS", "Payment Completed Successfully"));

		log.info("Published payment-completed event for transaction : {}", event.getTransactionId());

		log.info("Payment {} completed successfully", event.getTransactionId());
	}

	public List<TransactionHistoryResponse> getTransactions(Long userId) {

		log.info("Fetching transaction history for user : {}", userId);

		List<TransactionHistoryResponse> transactions = transactionRepository.findBySenderIdOrReceiverId(userId, userId)
				.stream().map(txn -> new TransactionHistoryResponse(txn.getTransactionId(), txn.getSenderId(),
						txn.getReceiverId(), txn.getAmount(), txn.getCurrency(), txn.getStatus().name()))
				.toList();

		log.info("Fetched {} transactions for user : {}", transactions.size(), userId);

		return transactions;
	}
}