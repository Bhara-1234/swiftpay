package com.swiftpay.ledgerservice.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swiftpay.ledgerservice.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	@Modifying
	@Query("""
			UPDATE Users u
			SET u.balance = u.balance - :amount
			WHERE u.id = :userId
			AND u.balance >= :amount
			""")
	int debitBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

	@Modifying
	@Query("""
			UPDATE Users u
			SET u.balance = u.balance + :amount
			WHERE u.id = :userId
			""")
	int creditBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
