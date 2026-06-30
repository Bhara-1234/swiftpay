package com.swiftpay.ledgerservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionHistoryResponse {

	private String transactionId;

	private Long senderId;

	private Long receiverId;

	private BigDecimal amount;

	private String currency;

	private String status;
}