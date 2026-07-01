package com.swiftpay.gatewayservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swiftpay.gatewayservice.entity.UserAccount;

public interface UserAccountRepository
extends JpaRepository<UserAccount, Long> {

}
