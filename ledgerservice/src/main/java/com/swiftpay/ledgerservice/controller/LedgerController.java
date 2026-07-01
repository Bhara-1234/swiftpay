package com.swiftpay.ledgerservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftpay.ledgerservice.dto.TransactionHistoryResponse;
import com.swiftpay.ledgerservice.service.LedgerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/ledger")
@RequiredArgsConstructor
@Slf4j
public class LedgerController {

	private final LedgerService ledgerService;

	@GetMapping("/transactions/{userId}")
	public List<TransactionHistoryResponse> getTransactions(@PathVariable Long userId) {

		log.debug("Entering getTransactions() with userId: {}", userId);

		List<TransactionHistoryResponse> transactions = ledgerService.getTransactions(userId);

		log.debug("Leaving getTransactions() with {} transactions for userId: {}", transactions.size(), userId);

		return transactions;
	}
}