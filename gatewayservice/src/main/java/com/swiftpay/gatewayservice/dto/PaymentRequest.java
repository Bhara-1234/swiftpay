package com.swiftpay.gatewayservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRequest {

	@NotBlank(message = "transactionId is mandatory")
	private String transactionId;

	@NotNull(message = "senderId is mandatory")
	private Long senderId;

	@NotNull(message = "receiverId is mandatory")
	private Long receiverId;

	@NotNull(message = "amount is mandatory")
	@DecimalMin(value = "0.01", message = "Amount must be greater than or equal to 0.01")
	private BigDecimal amount;

	@NotBlank(message = "currency is mandatory")
	private String currency;
}
