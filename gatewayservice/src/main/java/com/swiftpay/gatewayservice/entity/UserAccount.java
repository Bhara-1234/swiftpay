package com.swiftpay.gatewayservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserAccount {

	@Id
	@NotNull(message = "User id is required")
	@Column(nullable = false)
	private Long id;

	@NotBlank(message = "Name is required")
	@Column(nullable = false)
	private String name;

	@NotNull(message = "Balance is required")
	@DecimalMin(value = "0.0", message = "Balance cannot be negative")
	@Column(nullable = false)
	private BigDecimal balance;
}
