package com.swiftpay.ledgerservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.swiftpay.ledgerservice.enums.TransactionStatus;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId;

    private Long senderId;

    private Long receiverId;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "remarks")
    private String remarks;
    
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}