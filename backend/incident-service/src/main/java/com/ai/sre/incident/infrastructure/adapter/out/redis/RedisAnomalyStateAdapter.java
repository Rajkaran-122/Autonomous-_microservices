package com.ai.sre.incident.infrastructure.adapter.out.redis;

import com.ai.sre.incident.application.port.out.AnomalyStatePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisAnomalyStateAdapter implements AnomalyStatePort {

    private final StringRedisTemplate redisTemplate;

    public RedisAnomalyStateAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Long incrementErrorCount(String serviceName, Duration window) {
        String errorKey = "sre:errors:" + serviceName;
        Long errorCount = redisTemplate.opsForValue().increment(errorKey);
        if (errorCount != null && errorCount == 1) {
            redisTemplate.expire(errorKey, window);
        }
        return errorCount;
    }

    @Override
    public void resetErrorCount(String serviceName) {
        String errorKey = "sre:errors:" + serviceName;
        redisTemplate.delete(errorKey);
    }

    @Override
    public boolean isAlreadyDeduplicated(String serviceName) {
        String dedupKey = "sre:dedup:" + serviceName;
        return Boolean.TRUE.equals(redisTemplate.hasKey(dedupKey));
    }

    @Override
    public void markAsDeduplicated(String serviceName, Duration window) {
        String dedupKey = "sre:dedup:" + serviceName;
        redisTemplate.opsForValue().set(dedupKey, "1", window);
    }
}
