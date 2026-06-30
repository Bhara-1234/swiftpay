package com.swiftpay.ledgerservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import com.swiftpay.ledgerservice.event.PaymentEvent;
import com.swiftpay.ledgerservice.service.LedgerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

	private final LedgerService ledgerService;

	@RetryableTopic(attempts = "3", backoff = @org.springframework.retry.annotation.Backoff(delay = 5000))
	@KafkaListener(topics = "payment-initiated", groupId = "ledger-group")
	public void consume(PaymentEvent event) {

		log.info("Received Event : {}", event);

		ledgerService.processPayment(event);
	}
}