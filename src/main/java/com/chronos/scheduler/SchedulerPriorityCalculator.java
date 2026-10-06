package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class SchedulerPriorityCalculator {

    private final AgingPolicy agingPolicy;
    private final Clock clock;

    public SchedulerPriorityCalculator(
            AgingPolicy agingPolicy,
            Clock clock) {

        this.agingPolicy = agingPolicy;
        this.clock = clock;
    }

    public int calculateEffectivePriority(
            ScheduledJob job) {

        Instant queuedAt = job.getQueuedAt();

        if (queuedAt == null) {
            throw new IllegalStateException(
                    "Scheduled job has not been added to the queue yet."
            );
        }

        long waitingMillis =
                Duration.between(
                        queuedAt,
                        Instant.now(clock)
                ).toMillis();

        int agingBonus =
                agingPolicy.calculateBonus(
                        waitingMillis
                );

        return job.getPriority() + agingBonus;
    }
}