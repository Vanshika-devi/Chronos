package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobQueueTest {

    @Test
    void higherPriorityJobShouldBeReturnedFirst()
            throws InterruptedException {

        JobQueue queue = new JobQueue();

        ScheduledJob job1 =
                new ScheduledJob(
                        1L,
                        101L,
                        2
                );

        ScheduledJob job2 =
                new ScheduledJob(
                        2L,
                        102L,
                        10
                );

        ScheduledJob job3 =
                new ScheduledJob(
                        3L,
                        103L,
                        5
                );

        queue.add(job1);
        queue.add(job2);
        queue.add(job3);

        assertEquals(
                2L,
                queue.take().getJobId()
        );

        assertEquals(
                3L,
                queue.take().getJobId()
        );

        assertEquals(
                1L,
                queue.take().getJobId()
        );
    }
}