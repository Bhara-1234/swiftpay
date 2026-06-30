package com.swiftpay.ledgerservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {

	@NotNull
	private Long id;

	@NotBlank
	private String name;

	@NotNull
	@DecimalMin("0.00")
	private BigDecimal balance;
}