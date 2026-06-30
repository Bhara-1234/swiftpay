package com.swiftpay.ledgerservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swiftpay.ledgerservice.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByTransactionId(String transactionId);

	List<Transaction> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}