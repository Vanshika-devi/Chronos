package com.chronos.scheduler;

import com.chronos.service.JobExecutionService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SchedulerTest {

    @Test
    void schedulerShouldDispatchJobFromQueue() throws Exception {

        Clock clock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneId.of("UTC")
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

        JobQueue queue =
                new JobQueue(
                        calculator,
                        fairnessPolicy,
                        clock
                );

        JobExecutionService executionService =
                mock(JobExecutionService.class);

        Scheduler scheduler =
                new Scheduler(
                        queue,
                        executionService
                );

        ScheduledJob job =
                new ScheduledJob(
                        1L,
                        100L,
                        10,
                        1L,
                        "A"
                );

        scheduler.start();

        Thread.sleep(50);

        queue.add(job);

        Thread.sleep(100);

        verify(executionService)
                .dispatchJob(1L, 100L);

        scheduler.stop();

        assertTrue(!scheduler.isRunning());
    }
}