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
import com.swiftpay.gatewayservice.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<PaymentResponse> makePayment(@RequestBody @Valid PaymentRequest request) {

		PaymentResponse response = paymentService.processPayment(request);

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@GetMapping("/{transactionId}")
	public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {

		return ResponseEntity.ok(paymentService.getPaymentStatus(transactionId));
	}
}