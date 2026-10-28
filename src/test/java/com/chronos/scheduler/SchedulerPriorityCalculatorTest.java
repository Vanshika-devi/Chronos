package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerPriorityCalculatorTest {

    @Test
    void agingShouldIncreaseEffectivePriority() {

        Instant start =
                Instant.parse(
                        "2026-10-06T18:00:00Z"
                );

        Instant later =
                start.plusMillis(300);

        Clock clock =
                Clock.fixed(
                        later,
                        ZoneOffset.UTC
                );

        AgingPolicy agingPolicy =
                new AgingPolicy(
                        true,
                        100,
                        5
                );

        SchedulerPriorityCalculator calculator =
                new SchedulerPriorityCalculator(
                        agingPolicy,
                        clock
                );

        ScheduledJob job =
                new ScheduledJob(
                        1L,
                        101L,
                        1,
                        1L,
                        "DEFAULT"
                );

        job.markQueued(start);

        int effectivePriority =
                calculator.calculateEffectivePriority(
                        job
                );

        assertEquals(
                4,
                effectivePriority
        );
    }

    @Test
    void agingShouldRespectMaximumBonus() {

        Instant start =
                Instant.parse(
                        "2026-10-06T18:00:00Z"
                );

        Instant later =
                start.plusSeconds(10);

        Clock clock =
                Clock.fixed(
                        later,
                        ZoneOffset.UTC
                );

        AgingPolicy agingPolicy =
                new AgingPolicy(
                        true,
                        100,
                        5
                );

        SchedulerPriorityCalculator calculator =
                new SchedulerPriorityCalculator(
                        agingPolicy,
                        clock
                );

        ScheduledJob job =
                new ScheduledJob(
                        1L,
                        101L,
                        1,
                        1L,
                        "DEFAULT"
                );

        job.markQueued(start);

        int effectivePriority =
                calculator.calculateEffectivePriority(
                        job
                );

        assertEquals(
                6,
                effectivePriority
        );
    }
    @Test
    void noWaitingTimeShouldKeepOriginalPriority() {
        Instant now = Instant.parse(
                "2026-10-06T18:00:00Z"
        );

        Clock clock = Clock.fixed(
                now,
                ZoneOffset.UTC
        );

        AgingPolicy agingPolicy = new AgingPolicy(
                true,
                100,
                5
        );

        SchedulerPriorityCalculator calculator =
                new SchedulerPriorityCalculator(
                        agingPolicy,
                        clock
                );

        ScheduledJob job = new ScheduledJob(
                1L,
                101L,
                5,
                1L,
                "DEFAULT"
        );

        job.markQueued(now);

        assertEquals(
                5,
                calculator.calculateEffectivePriority(job)
        );
    }
    @Test
    void disabledAgingShouldKeepOriginalPriority() {
        Instant queuedAt = Instant.parse(
                "2026-10-06T18:00:00Z"
        );

        Instant later = queuedAt.plusSeconds(100);

        Clock clock = Clock.fixed(
                later,
                ZoneOffset.UTC
        );

        AgingPolicy agingPolicy = new AgingPolicy(
                false,
                100,
                5
        );

        SchedulerPriorityCalculator calculator =
                new SchedulerPriorityCalculator(
                        agingPolicy,
                        clock
                );

        ScheduledJob job = new ScheduledJob(
                1L,
                101L,
                3,
                1L,
                "DEFAULT"
        );

        job.markQueued(queuedAt);

        assertEquals(
                3,
                calculator.calculateEffectivePriority(job)
        );
    }
}