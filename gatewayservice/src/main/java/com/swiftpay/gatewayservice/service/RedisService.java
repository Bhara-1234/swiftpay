package com.swiftpay.gatewayservice.service;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final StringRedisTemplate redisTemplate;

	public boolean isDuplicate(String transactionId) {

		Boolean inserted = redisTemplate.opsForValue().setIfAbsent("payment:" + transactionId, "PROCESSED",
				Duration.ofHours(24));

		return Boolean.FALSE.equals(inserted);
	}
}