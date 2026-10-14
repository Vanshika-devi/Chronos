package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class JobWorkerTest {

    @Test
    void workerShouldExecuteQueuedJob() throws Exception {

        JobQueue queue = mock(JobQueue.class);
        JobExecutor executor = mock(JobExecutor.class);

        ScheduledJob job =
                new ScheduledJob(
                        1L,
                        1L,
                        10,
                        1L,
                        "A"
                );

        when(queue.take())
                .thenReturn(job)
                .thenThrow(new InterruptedException());

        JobWorker worker =
                new JobWorker(queue, executor);

        worker.start();

        verify(executor, timeout(1000))
                .execute(job);

        worker.shutdown();
    }
}