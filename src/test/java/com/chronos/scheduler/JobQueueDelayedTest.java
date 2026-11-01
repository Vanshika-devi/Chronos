
package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueDelayedTest {

    private JobQueue createQueue(TestClock clock) {

        AgingPolicy agingPolicy =
                new AgingPolicy(true, 100, 5);

        SchedulerPriorityCalculator calculator =
                new SchedulerPriorityCalculator(
                        agingPolicy,
                        clock
                );

        FairnessPolicy fairnessPolicy =
                new FairnessPolicy(2);

        return new JobQueue(
                calculator,
                fairnessPolicy,
                clock
        );
    }

    private ScheduledJob createJob(
            long jobId,
            int priority,
            long sequence,
            String group) {

        return new ScheduledJob(
                jobId,
                jobId + 100,
                priority,
                sequence,
                group
        );
    }

    @Test
    void delayedJobShouldNotBeAvailableImmediately() {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ScheduledJob job =
                createJob(1L, 10, 1L, "A");

        queue.addDelayed(job, Duration.ofSeconds(5));

        assertNull(
                queue.peek(),
                "Delayed job must not be available immediately"
        );

        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void delayedJobShouldBecomeAvailableAfterDelay()
            throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ScheduledJob delayedJob =
                createJob(1L, 10, 1L, "A");

        queue.addDelayed(
                delayedJob,
                Duration.ofSeconds(5)
        );

        assertNull(queue.peek());

        // Advance simulated time by five seconds.
        clock.advanceMillis(5000);

        // Adding a job signals the queue condition.
        // Give this signal job lower priority.
        queue.add(
                createJob(2L, 1, 2L, "B")
        );

        // The previously delayed high-priority job
        // should now be eligible and selected first.
        ScheduledJob selected = queue.take();

        assertEquals(1L, selected.getJobId());
        assertEquals(1, queue.size());
    }

    @Test
    void normalJobShouldNotBeBlockedByDelayedJob()
            throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ScheduledJob delayedJob =
                createJob(1L, 10, 1L, "A");

        ScheduledJob normalJob =
                createJob(2L, 5, 2L, "B");

        queue.addDelayed(
                delayedJob,
                Duration.ofSeconds(30)
        );

        queue.add(normalJob);

        ScheduledJob selected = queue.take();

        assertEquals(
                2L,
                selected.getJobId(),
                "Normal eligible job should execute without waiting"
        );

        assertEquals(1, queue.size());
        assertNull(queue.peek());
    }

    @Test
    void delayedJobShouldRespectPriorityAfterBecomingAvailable()
            throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ScheduledJob delayedHighPriority =
                createJob(1L, 10, 1L, "A");

        ScheduledJob normalLowPriority =
                createJob(2L, 1, 2L, "B");

        queue.addDelayed(
                delayedHighPriority,
                Duration.ofSeconds(3)
        );

        clock.advanceMillis(3000);

        queue.add(normalLowPriority);

        assertEquals(
                1L,
                queue.take().getJobId()
        );

        assertEquals(
                2L,
                queue.take().getJobId()
        );
    }

    @Test
    void delayedJobShouldBeRemovedAfterExecution()
            throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ScheduledJob job =
                createJob(1L, 10, 1L, "A");

        queue.addDelayed(
                job,
                Duration.ofSeconds(2)
        );

        clock.advanceMillis(2000);

        // Signal the queue after advancing the test clock.
        queue.add(
                createJob(2L, 1, 2L, "B")
        );

        assertEquals(1L, queue.take().getJobId());

        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }

    private static class TestClock extends Clock {

        private Instant currentTime;

        TestClock(Instant initialTime) {
            this.currentTime = initialTime;
        }

        void advanceMillis(long millis) {
            currentTime = currentTime.plusMillis(millis);
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
}
