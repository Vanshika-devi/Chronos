package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.scheduler.JobQueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JobCancellationIntegrationTest {

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private JobQueue jobQueue;

    @Test
    void shouldCancelQueuedJobAndExecution() {

        // Arrange: create a job
        Job job = new Job(
                null,
                "Cancellation Test",
                5,
                JobStatus.CREATED
        );

        job = jobRepository.save(job);

        Long jobId = job.getId();

        // Act: queue the job
        jobExecutionService.queueJob(jobId);

        // Cancel before the scheduler takes it
        jobExecutionService.cancelQueuedJob(jobId);

        // Assert: job should be cancelled
        Job cancelledJob = jobRepository.findById(jobId)
                .orElseThrow();

        assertEquals(
                JobStatus.CANCELLED,
                cancelledJob.getStatus()
        );

        // Assert: execution should be cancelled
        Execution execution = executionRepository
                .findByJob_Id(jobId)
                .get(0);

        assertEquals(
                ExecutionStatus.CANCELLED,
                execution.getStatus()
        );

        assertNotNull(execution.getFinishedAt());

        // Assert: job should not remain in the queue
        assertFalse(
                jobQueue.remove(jobId, execution.getId())
        );
    }
}