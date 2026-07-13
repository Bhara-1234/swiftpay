package com.swiftpay.ledgerservice.util;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import com.swiftpay.ledgerservice.entity.Transaction;
import com.swiftpay.ledgerservice.entity.UserAccount;
import com.swiftpay.ledgerservice.event.PaymentEvent;
import com.swiftpay.ledgerservice.repository.TransactionRepository;
import com.swiftpay.ledgerservice.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LedgerValidationUtil {

	private final UserAccountRepository userRepository;

	private final TransactionRepository transactionRepository;

	public Transaction validateTransaction(PaymentEvent event) {

		return transactionRepository.findByTransactionId(event.getTransactionId())
				.orElseThrow(() -> new ResourceNotFoundException("Transaction Not Found"));
	}

	public UserAccount validateSender(PaymentEvent event) {

		return userRepository.findById(event.getSenderId())
				.orElseThrow(() -> new ResourceNotFoundException("Sender Account Not Found"));
	}

	public UserAccount validateReceiver(PaymentEvent event) {

		return userRepository.findById(event.getReceiverId())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver Account Not Found"));
	}

}