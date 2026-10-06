package com.chronos.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AgingPolicy {

    private final boolean enabled;
    private final long quantumMillis;
    private final int maxBonus;

    public AgingPolicy(
            @Value("${chronos.scheduler.aging.enabled:true}")
            boolean enabled,

            @Value("${chronos.scheduler.aging.quantum-ms:100}")
            long quantumMillis,

            @Value("${chronos.scheduler.aging.max-bonus:5}")
            int maxBonus) {

        if (quantumMillis <= 0) {
            throw new IllegalArgumentException(
                    "Aging quantum must be greater than zero."
            );
        }

        if (maxBonus < 0) {
            throw new IllegalArgumentException(
                    "Maximum aging bonus cannot be negative."
            );
        }

        this.enabled = enabled;
        this.quantumMillis = quantumMillis;
        this.maxBonus = maxBonus;
    }

    public int calculateBonus(long waitingMillis) {

        if (!enabled || waitingMillis <= 0) {
            return 0;
        }

        long bonus =
                waitingMillis / quantumMillis;

        return (int) Math.min(
                bonus,
                maxBonus
        );
    }
}