package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueTest {

    private JobQueue createQueue() {

        Clock clock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

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
    void newQueueShouldBeEmpty() {

        JobQueue queue = createQueue();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void addingJobShouldIncreaseSize() {

        JobQueue queue = createQueue();

        queue.add(
                createJob(1L, 5, 1L, "A")
        );

        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
    }

    @Test
    void addingMultipleJobsShouldTrackCorrectSize() {

        JobQueue queue = createQueue();

        queue.add(createJob(1L, 5, 1L, "A"));
        queue.add(createJob(2L, 5, 2L, "B"));
        queue.add(createJob(3L, 5, 3L, "C"));

        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void takingJobShouldRemoveItFromQueue() throws Exception {

        JobQueue queue = createQueue();

        ScheduledJob job =
                createJob(1L, 5, 1L, "A");

        queue.add(job);

        ScheduledJob selected = queue.take();

        assertEquals(job, selected);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void peekShouldNotRemoveJob() {

        JobQueue queue = createQueue();

        ScheduledJob job =
                createJob(1L, 5, 1L, "A");

        queue.add(job);

        ScheduledJob peeked = queue.peek();

        assertEquals(job, peeked);
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void peekOnEmptyQueueShouldReturnNull() {

        JobQueue queue = createQueue();

        assertNull(queue.peek());
    }

    @Test
    void takingJobsShouldRespectPriority() throws Exception {

        JobQueue queue = createQueue();

        ScheduledJob low =
                createJob(1L, 1, 1L, "A");

        ScheduledJob high =
                createJob(2L, 10, 2L, "A");

        queue.add(low);
        queue.add(high);

        assertEquals(
                2L,
                queue.take().getJobId()
        );

        assertEquals(
                1L,
                queue.take().getJobId()
        );
    }

    @Test
    void samePriorityShouldRespectSequenceNumber()
            throws Exception {

        JobQueue queue = createQueue();

        ScheduledJob newer =
                createJob(2L, 5, 2L, "A");

        ScheduledJob older =
                createJob(1L, 5, 1L, "A");

        queue.add(newer);
        queue.add(older);

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
    void addingNullJobShouldBeRejected() {

        JobQueue queue = createQueue();

        assertThrows(
                IllegalArgumentException.class,
                () -> queue.add(null)
        );

        assertEquals(0, queue.size());
    }

    @Test
    void takingAllJobsShouldEventuallyEmptyQueue()
            throws Exception {

        JobQueue queue = createQueue();

        queue.add(createJob(1L, 5, 1L, "A"));
        queue.add(createJob(2L, 5, 2L, "A"));
        queue.add(createJob(3L, 5, 3L, "A"));

        queue.take();
        queue.take();
        queue.take();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }
}