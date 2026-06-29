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
    public boolean tryAcquireDedupLock(String serviceName, Duration window) {
        String dedupKey = "sre:dedup:" + serviceName;
        // setIfAbsent is an atomic SETNX operation
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", window));
    }

    @Override
    public void recordErrorMinute(String serviceName, long minuteEpoch) {
        String historyKey = "sre:history:" + serviceName;
        redisTemplate.opsForHash().increment(historyKey, String.valueOf(minuteEpoch), 1);
        // Ensure the history expires after a few hours to prevent bloat
        redisTemplate.expire(historyKey, Duration.ofHours(3));
    }

    @Override
    public java.util.List<Long> getHistoricalErrorCounts(String serviceName, long currentMinuteEpoch, int maxBuckets) {
        String historyKey = "sre:history:" + serviceName;
        java.util.List<Long> counts = new java.util.ArrayList<>();
        
        // Fetch up to maxBuckets of previous minutes
        for (int i = 1; i <= maxBuckets; i++) {
            long targetMinute = currentMinuteEpoch - i;
            Object val = redisTemplate.opsForHash().get(historyKey, String.valueOf(targetMinute));
            if (val != null) {
                counts.add(Long.parseLong(val.toString()));
            } else {
                counts.add(0L); // No errors in that minute
            }
        }
        return counts;
    }
}
