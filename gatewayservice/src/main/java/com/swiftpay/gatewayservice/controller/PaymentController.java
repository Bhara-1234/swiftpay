package com.swiftpay.gatewayservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftpay.gatewayservice.dto.ErrorResponse;
import com.swiftpay.gatewayservice.dto.PaymentRequest;
import com.swiftpay.gatewayservice.dto.PaymentResponse;
import com.swiftpay.gatewayservice.entity.UserAccount;
import com.swiftpay.gatewayservice.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment APIs", description = "APIs for payment initiation, payment status tracking and user account creation")
public class PaymentController {

	private final PaymentService paymentService;

	@Operation(summary = "Initiate Payment", description = "Validates the payment request, stores the transaction with PENDING status and publishes a PaymentEvent to Kafka for asynchronous processing.")
	@ApiResponses({
			@ApiResponse(responseCode = "202", description = "Payment accepted for asynchronous processing", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid payment request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "Sender or Receiver account not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "Duplicate transaction", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping
	public ResponseEntity<PaymentResponse> makePayment(@RequestBody @Valid PaymentRequest request) {

		log.debug("Entering makePayment() with transactionId: {}", request.getTransactionId());

		PaymentResponse response = paymentService.processPayment(request);

		log.debug("Leaving makePayment() with transactionId: {}", request.getTransactionId());

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@Operation(summary = "Get Payment Status", description = "Returns the current status of a payment transaction using the transaction ID.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Payment status retrieved successfully", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
			@ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/{transactionId}")
	public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {

		log.debug("Entering getPaymentStatus() with transactionId: {}", transactionId);

		PaymentResponse response = paymentService.getPaymentStatus(transactionId);

		log.debug("Leaving getPaymentStatus() with transactionId: {}", transactionId);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Create User Account", description = "Creates a new user account that can participate in payment transactions.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User account created successfully", content = @Content(schema = @Schema(implementation = UserAccount.class))),
			@ApiResponse(responseCode = "409", description = "User already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping("/createUser")
	public ResponseEntity<UserAccount> createUser(@RequestBody @Valid UserAccount user) {

		log.debug("Entering createUser() with userId: {}", user.getId());

		UserAccount createdUser = paymentService.createUser(user);

		log.debug("Leaving createUser() with userId: {}", user.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}
}