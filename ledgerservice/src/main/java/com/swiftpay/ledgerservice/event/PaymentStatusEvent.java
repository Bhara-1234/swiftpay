package com.swiftpay.ledgerservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent {

	private String transactionId;
	private String status;
	private String message;
}