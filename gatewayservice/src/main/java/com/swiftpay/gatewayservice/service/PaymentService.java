package com.swiftpay.gatewayservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.swiftpay.gatewayservice.dto.PaymentRequest;
import com.swiftpay.gatewayservice.dto.PaymentResponse;
import com.swiftpay.gatewayservice.entity.Transaction;
import com.swiftpay.gatewayservice.enums.TransactionStatus;
import com.swiftpay.gatewayservice.event.PaymentEvent;
import com.swiftpay.gatewayservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final TransactionRepository repository;
	private final RedisService redisService;

	private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

	public PaymentResponse processPayment(PaymentRequest request) {

		if (redisService.isDuplicate(request.getTransactionId())) {

			throw new RuntimeException("Duplicate Transaction");
		}
		Transaction transaction = new Transaction();

		transaction.setTransactionId(request.getTransactionId());

		transaction.setSenderId(request.getSenderId());

		transaction.setReceiverId(request.getReceiverId());

		transaction.setAmount(request.getAmount());

		transaction.setCurrency(request.getCurrency());

		transaction.setStatus(TransactionStatus.PENDING);

		repository.save(transaction);

		PaymentEvent event = new PaymentEvent(request.getTransactionId(), request.getSenderId(),
				request.getReceiverId(), request.getAmount(), request.getCurrency());

		kafkaTemplate.send("payment-initiated", event);

		return new PaymentResponse(request.getTransactionId(), "PENDING", "Payment accepted for processing");
	}

	public PaymentResponse getPaymentStatus(String transactionId) {

		Transaction txn = repository.findByTransactionId(transactionId).orElseThrow(() -> new RuntimeException("Transaction Not Found"));

		return new PaymentResponse(txn.getTransactionId(), txn.getStatus().name(), "Transaction status fetched successfully");
	}
}