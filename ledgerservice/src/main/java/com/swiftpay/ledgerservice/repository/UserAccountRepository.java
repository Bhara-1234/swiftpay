package com.swiftpay.ledgerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swiftpay.ledgerservice.entity.UserAccount;

public interface UserAccountRepository
extends JpaRepository<UserAccount, Long> {

}
