package com.swiftpay.gatewayservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {

    private String transactionId;
    private String status;
    private String message;
}