package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobQueueAgingTest {

    @Test
    void olderLowPriorityJobShouldOvertakeNewHighPriorityJob()
            throws InterruptedException {

        Instant start =
                Instant.parse(
                        "2026-10-06T18:00:00Z"
                );

        TestClock clock =
                new TestClock(start);

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

        FairnessPolicy fairnessPolicy =
                new FairnessPolicy(2);

        JobQueue queue =
                new JobQueue(
                        calculator,
                        fairnessPolicy,
                        clock
                );

        /*
         * Job 1:
         *
         * Priority = 1
         * Arrives first
         */
        ScheduledJob oldLowPriorityJob =
                new ScheduledJob(
                        1L,
                        101L,
                        1,
                        1L,
                        "DEFAULT"
                );

        queue.add(oldLowPriorityJob);

        /*
         * Move simulated time forward by 600 ms.
         */
        clock.advanceMillis(600);

        /*
         * Job 2:
         *
         * Priority = 5
         * Arrives later
         */
        ScheduledJob newHighPriorityJob =
                new ScheduledJob(
                        2L,
                        102L,
                        5,
                        2L,
                        "DEFAULT"
                );

        queue.add(newHighPriorityJob);

        /*
         * Old P1:
         *
         * Base priority = 1
         * Waiting time = 600 ms
         * Aging quantum = 100 ms
         * Aging bonus = 5 (capped)
         *
         * Effective priority = 6
         *
         * New P5:
         *
         * Waiting time = 0 ms
         * Aging bonus = 0
         *
         * Effective priority = 5
         *
         * Therefore P1 should win.
         */
        ScheduledJob selected =
                queue.take();

        assertEquals(
                oldLowPriorityJob.getJobId(),
                selected.getJobId()
        );
    }

    private static class TestClock extends Clock {

        private Instant currentTime;

        TestClock(Instant initialTime) {
            this.currentTime = initialTime;
        }

        void advanceMillis(long millis) {
            currentTime =
                    currentTime.plusMillis(millis);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return currentTime;
        }
    }

    @Test
    void sameEffectivePriorityShouldUseSequenceNumber()
            throws InterruptedException {

        Instant start =
                Instant.parse(
                        "2026-10-06T18:00:00Z"
                );

        TestClock clock =
                new TestClock(start);

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

        FairnessPolicy fairnessPolicy =
                new FairnessPolicy(2);

        JobQueue queue =
                new JobQueue(
                        calculator,
                        fairnessPolicy,
                        clock
                );

        /*
         * Job A:
         *
         * Base priority = 1
         * Sequence = 1
         */
        ScheduledJob olderJob =
                new ScheduledJob(
                        1L,
                        101L,
                        1,
                        1L,
                        "DEFAULT"
                );

        queue.add(olderJob);

        /*
         * Wait 400 ms in simulated time.
         *
         * Aging bonus = 4
         *
         * Effective priority:
         *
         * 1 + 4 = 5
         */
        clock.advanceMillis(400);

        /*
         * Job B:
         *
         * Base priority = 5
         * Sequence = 2
         *
         * It arrives later, so it has
         * almost no aging.
         */
        ScheduledJob newerJob =
                new ScheduledJob(
                        2L,
                        102L,
                        5,
                        2L,
                        "DEFAULT"
                );

        queue.add(newerJob);

        /*
         * Both jobs now have effective priority 5.
         *
         * Therefore sequence number decides.
         *
         * Job A → sequence 1
         * Job B → sequence 2
         *
         * Job A should win.
         */
        ScheduledJob selected =
                queue.take();

        assertEquals(
                olderJob.getJobId(),
                selected.getJobId()
        );
    }
}