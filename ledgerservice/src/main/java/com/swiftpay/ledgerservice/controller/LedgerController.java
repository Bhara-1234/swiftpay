package com.swiftpay.ledgerservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftpay.ledgerservice.dto.ErrorResponse;
import com.swiftpay.ledgerservice.dto.TransactionHistoryResponse;
import com.swiftpay.ledgerservice.service.LedgerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/ledger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ledger APIs", description = "APIs for retrieving payment transaction history")
public class LedgerController {

	private final LedgerService ledgerService;

	@Operation(summary = "Get Transaction History", description = "Returns all transactions where the specified user is either the sender or the receiver.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionHistoryResponse.class)))),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	@GetMapping("/transactions/{userId}")
	public ResponseEntity<List<TransactionHistoryResponse>> getTransactions(@PathVariable Long userId) {

		log.debug("Entering getTransactions() with userId: {}", userId);

		List<TransactionHistoryResponse> transactions = ledgerService.getTransactions(userId);

		log.debug("Leaving getTransactions() with {} transactions for userId: {}", transactions.size(), userId);

		return ResponseEntity.ok(transactions);
	}
}