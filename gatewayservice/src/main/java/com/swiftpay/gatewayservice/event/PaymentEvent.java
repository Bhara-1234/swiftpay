package com.swiftpay.gatewayservice.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String transactionId;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String currency;
}
