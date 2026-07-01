package com.swiftpay.gatewayservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.swiftpay.gatewayservice.service.RedisService;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @Test
    void isDuplicate_ShouldReturnFalse_WhenInserted() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(anyString(), anyString(), any()))
                .thenReturn(true);

        boolean result = redisService.isDuplicate("TXN001");

        assertFalse(result);
    }

    @Test
    void isDuplicate_ShouldReturnTrue_WhenAlreadyExists() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(anyString(), anyString(), any()))
                .thenReturn(false);

        boolean result = redisService.isDuplicate("TXN001");

        assertTrue(result);
    }
}