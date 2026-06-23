package com.ai.sre.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Feature Flag Engine — Evaluates dynamic feature flags using consistent hashing
 * for percentage-based canary rollouts, and provides emergency kill switches.
 */
@Service
public class FeatureFlagService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);

    private final StringRedisTemplate redisTemplate;
    
    private static final String FLAG_PREFIX = "sre:feature-flag:";

    public FeatureFlagService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Set a feature flag configuration.
     * @param flagName Name of the feature flag
     * @param enabled True if globally enabled
     * @param percentage Rollout percentage (0-100)
     */
    public void configureFlag(String flagName, boolean enabled, int percentage) {
        redisTemplate.opsForHash().put(FLAG_PREFIX + flagName, "enabled", String.valueOf(enabled));
        redisTemplate.opsForHash().put(FLAG_PREFIX + flagName, "percentage", String.valueOf(percentage));
        log.info("Configured feature flag '{}': enabled={}, percentage={}%", flagName, enabled, percentage);
    }

    /**
     * Emergency kill switch to immediately disable a feature.
     */
    public void emergencyKillSwitch(String flagName) {
        configureFlag(flagName, false, 0);
        log.warn("🚨 Emergency Kill Switch activated for feature flag: {}", flagName);
    }

    /**
     * Evaluates if a feature is enabled for a specific user using consistent hashing.
     */
    public boolean isFeatureEnabled(String flagName, String userId) {
        Object enabledObj = redisTemplate.opsForHash().get(FLAG_PREFIX + flagName, "enabled");
        Object percentageObj = redisTemplate.opsForHash().get(FLAG_PREFIX + flagName, "percentage");

        if (enabledObj == null) {
            return false; // Default off if not configured
        }

        boolean isEnabled = Boolean.parseBoolean(enabledObj.toString());
        if (!isEnabled) {
            return false;
        }

        int rolloutPercentage = percentageObj != null ? Integer.parseInt(percentageObj.toString()) : 100;
        
        if (rolloutPercentage == 100) return true;
        if (rolloutPercentage == 0) return false;

        // Consistent hashing for percentage rollout
        int userHash = getConsistentHash(userId + flagName);
        return (userHash % 100) < rolloutPercentage;
    }

    private int getConsistentHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            // Use the first 4 bytes to create an integer
            return Math.abs((digest[0] & 0xFF) |
                    ((digest[1] & 0xFF) << 8) |
                    ((digest[2] & 0xFF) << 16) |
                    ((digest[3] & 0xFF) << 24));
        } catch (NoSuchAlgorithmException e) {
            return Math.abs(input.hashCode());
        }
    }
}
