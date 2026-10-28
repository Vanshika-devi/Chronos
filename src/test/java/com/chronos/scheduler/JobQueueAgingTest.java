package com.chronos.scheduler;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobQueueAgingTest {

    @Test
    void olderLowPriorityJobShouldBeatNewHighPriorityJob() throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ScheduledJob oldJob = createJob(
                1L, 1L, 1, 1L, "A"
        );

        queue.add(oldJob);

        // Age the first job by 600 milliseconds.
        clock.advanceMillis(600);

        ScheduledJob newJob = createJob(
                2L, 2L, 5, 2L, "B"
        );

        queue.add(newJob);

        // P1 + maximum aging bonus 5 = effective P6.
        assertEquals(1L, queue.take().getJobId());
    }

    @Test
    void sameEffectivePriorityShouldUseSequenceNumber() throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        queue.add(createJob(
                2L, 2L, 5, 2L, "A"
        ));

        queue.add(createJob(
                1L, 1L, 5, 1L, "A"
        ));

        assertEquals(1L, queue.take().getJobId());
    }

    @Test
    void higherPriorityShouldBeatFairnessAcrossGroups() throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        // Establish A as the last selected group.
        queue.add(createJob(
                1L, 1L, 10, 1L, "A"
        ));

        assertEquals(1L, queue.take().getJobId());

        // B has lower priority and must not jump ahead.
        queue.add(createJob(
                2L, 2L, 5, 2L, "B"
        ));

        queue.add(createJob(
                3L, 3L, 10, 3L, "A"
        ));

        assertEquals(3L, queue.take().getJobId());
        assertEquals(2L, queue.take().getJobId());
    }

    @Test
    void fairnessShouldChooseAnotherGroupAmongEqualPriorityCandidates() throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        // Establish A as the previously selected group twice.
        queue.add(createJob(
                1L, 1L, 10, 1L, "A"
        ));
        assertEquals(1L, queue.take().getJobId());

        queue.add(createJob(
                2L, 2L, 10, 2L, "A"
        ));
        assertEquals(2L, queue.take().getJobId());

        // Now A has reached its consecutive-selection limit.
        queue.add(createJob(
                3L, 3L, 10, 3L, "A"
        ));
        queue.add(createJob(
                4L, 4L, 10, 4L, "B"
        ));

        assertEquals(4L, queue.take().getJobId());
    }

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
            Long jobId,
            Long executionId,
            int priority,
            long sequence,
            String group) {

        return new ScheduledJob(
                jobId,
                executionId,
                priority,
                sequence,
                group
        );
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
    @Test
    void repeatedPeekShouldNotAdvanceFairnessState() throws Exception {

        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        queue.add(createJob(1L, 1L, 10, 1L, "A"));
        assertEquals(1L, queue.take().getJobId());

        queue.add(createJob(2L, 2L, 10, 2L, "A"));
        assertEquals(2L, queue.take().getJobId());

        queue.add(createJob(3L, 3L, 10, 3L, "A"));
        queue.add(createJob(4L, 4L, 10, 4L, "B"));

        // A has reached its consecutive-selection limit.
        // Previewing the queue must not change fairness state.
        assertEquals(4L, queue.peek().getJobId());
        assertEquals(4L, queue.peek().getJobId());

        // The actual selection should still be B.
        assertEquals(4L, queue.take().getJobId());
    }
    @Test
    void emptyQueueShouldReturnNullOnPeek() {
        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        assertEquals(null, queue.peek());
    }

    @Test
    void emptyQueueShouldReportIsEmpty() {
        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        assertEquals(true, queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void addingJobShouldIncreaseQueueSize() {
        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        assertEquals(0, queue.size());

        queue.add(createJob(
                1L, 1L, 5, 1L, "A"
        ));

        assertEquals(1, queue.size());
        assertEquals(false, queue.isEmpty());
    }

    @Test
    void takingJobShouldRemoveItFromQueue() throws Exception {
        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        queue.add(createJob(
                1L, 1L, 5, 1L, "A"
        ));

        assertEquals(1, queue.size());

        assertEquals(1L, queue.take().getJobId());

        assertEquals(0, queue.size());
        assertEquals(true, queue.isEmpty());
    }

    @Test
    void addingNullJobShouldBeRejected() {
        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        assertThrows(
                IllegalArgumentException.class,
                () -> queue.add(null)
        );

        assertEquals(0, queue.size());
    }

    @Test
    void takeShouldWaitUntilJobIsAdded() throws Exception {
        TestClock clock = new TestClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        JobQueue queue = createQueue(clock);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<ScheduledJob> future = executor.submit(queue::take);

            // Give the take() thread a moment to enter the waiting state.
            Thread.sleep(100);

            assertEquals(false, future.isDone());

            queue.add(createJob(
                    1L,
                    1L,
                    5,
                    1L,
                    "A"
            ));

            ScheduledJob result =
                    future.get(1, TimeUnit.SECONDS);

            assertEquals(1L, result.getJobId());

        } finally {
            executor.shutdownNow();
        }
    }
}