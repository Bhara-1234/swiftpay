package com.swiftpay.gatewayservice.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swiftpay.gatewayservice.dto.PaymentRequest;
import com.swiftpay.gatewayservice.exception.DuplicateTransactionException;
import com.swiftpay.gatewayservice.exception.InvalidPaymentException;
import com.swiftpay.gatewayservice.exception.ResourceNotFoundException;
import com.swiftpay.gatewayservice.repository.TransactionRepository;
import com.swiftpay.gatewayservice.repository.UserAccountRepository;
import com.swiftpay.gatewayservice.service.RedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentValidationUtil {

	private final RedisService redisService;

	private final UserAccountRepository userRepository;

	private final TransactionRepository transactionRepository;

	public void validatePaymentRequest(PaymentRequest request) {

		validateDuplicateTransaction(request);

		validateSenderAndReceiver(request);

		validateCurrency(request);

		validateUsers(request);
	}

	private void validateDuplicateTransaction(PaymentRequest request) {

		if (redisService.isDuplicate(request.getTransactionId()) || transactionRepository.findByTransactionId(request.getTransactionId()).isPresent()) {

			throw new DuplicateTransactionException("Duplicate Transaction");
		}
	}

	private void validateSenderAndReceiver(PaymentRequest request) {

		if (request.getSenderId().equals(request.getReceiverId())) {

			throw new InvalidPaymentException("Sender and Receiver cannot be same");
		}
	}

	private void validateCurrency(PaymentRequest request) {

		List<String> supportedCurrencies = List.of("INR", "USD", "EUR");

		if (!supportedCurrencies.contains(request.getCurrency().toUpperCase())) {

			throw new InvalidPaymentException("Unsupported Currency");
		}
	}

	private void validateUsers(PaymentRequest request) {

		userRepository.findById(request.getSenderId())
				.orElseThrow(() -> new ResourceNotFoundException("Sender Account Not Found"));

		userRepository.findById(request.getReceiverId())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver Account Not Found"));
	}
}