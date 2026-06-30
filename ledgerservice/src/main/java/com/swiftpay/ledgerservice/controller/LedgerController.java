package com.swiftpay.ledgerservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftpay.ledgerservice.dto.TransactionHistoryResponse;
import com.swiftpay.ledgerservice.entity.UserAccount;
import com.swiftpay.ledgerservice.service.LedgerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

	private final LedgerService ledgerService;

	@GetMapping("/transactions/{userId}")
	public List<TransactionHistoryResponse> getTransactions(@PathVariable Long userId) {

		return ledgerService.getTransactions(userId);
	}

	@PostMapping("/users")
	public UserAccount createUser(@RequestBody UserAccount user) {

		return ledgerService.createUser(user);
	}
}