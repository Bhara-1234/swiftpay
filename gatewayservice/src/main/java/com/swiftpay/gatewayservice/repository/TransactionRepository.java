package com.swiftpay.gatewayservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.swiftpay.gatewayservice.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByTransactionId(String transactionId);
}