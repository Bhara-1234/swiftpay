package com.swiftpay.gatewayservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftpay.gatewayservice.dto.PaymentRequest;
import com.swiftpay.gatewayservice.dto.PaymentResponse;
import com.swiftpay.gatewayservice.entity.UserAccount;
import com.swiftpay.gatewayservice.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<PaymentResponse> makePayment(@RequestBody @Valid PaymentRequest request) {

		log.debug("Entering makePayment() with transactionId: {}", request.getTransactionId());

		PaymentResponse response = paymentService.processPayment(request);

		log.debug("Leaving makePayment() with transactionId: {}", request.getTransactionId());

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@GetMapping("/{transactionId}")
	public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {

		log.debug("Entering getPaymentStatus() with transactionId: {}", transactionId);

		PaymentResponse response = paymentService.getPaymentStatus(transactionId);

		log.debug("Leaving getPaymentStatus() with transactionId: {}", transactionId);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/createUser")
	public UserAccount createUser(@RequestBody UserAccount user) {

		log.debug("Entering createUser() with userId: {}", user.getId());

		UserAccount createdUser = paymentService.createUser(user);

		log.debug("Leaving createUser() with userId: {}", user.getId());

		return createdUser;
	}
}