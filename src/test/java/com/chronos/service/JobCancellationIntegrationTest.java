
package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.scheduler.JobQueue;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.scheduler.SchedulerService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobCancellationIntegrationTest {

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private JobQueue jobQueue;

    // Prevent the background scheduler from consuming jobs in this test.
    @MockBean
    private SchedulerService schedulerService;

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

        // Act 1: queue the job
        jobExecutionService.queueJob(jobId);

        Execution execution = executionRepository
                .findByJob_Id(jobId)
                .stream()
                .findFirst()
                .orElseThrow();

        // Verify that the afterCommit callback inserted the job.
        ScheduledJob queuedJob = jobQueue.peek();

        assertNotNull(queuedJob);
        assertEquals(jobId, queuedJob.getJobId());
        assertEquals(execution.getId(), queuedJob.getExecutionId());

        // Act 2: cancel the queued job
        jobExecutionService.cancelQueuedJob(jobId);

        // Assert: job should be cancelled
        Job cancelledJob = jobRepository.findById(jobId)
                .orElseThrow();

        assertEquals(
                JobStatus.CANCELLED,
                cancelledJob.getStatus()
        );

        // Assert: execution should be cancelled
        Execution cancelledExecution = executionRepository
                .findByJob_Id(jobId)
                .stream()
                .findFirst()
                .orElseThrow();

        assertEquals(
                ExecutionStatus.CANCELLED,
                cancelledExecution.getStatus()
        );

        assertNotNull(cancelledExecution.getFinishedAt());

        // Assert: cancelled item must no longer exist in the queue
        assertFalse(
                jobQueue.remove(jobId, execution.getId())
        );
    }

    @Autowired
    private ExecutionService executionService;

    @Test
    void shouldPreventCancelledJobFromStarting() {

        // Arrange
        Job job = new Job(
                null,
                "Cancelled Execution Guard Test",
                5,
                JobStatus.CREATED
        );

        job = jobRepository.save(job);
        Long jobId = job.getId();

        jobExecutionService.queueJob(jobId);

        Execution execution = executionRepository
                .findByJob_Id(jobId)
                .stream()
                .findFirst()
                .orElseThrow();

        Long executionId = execution.getId();

        // Cancel the queued job
        jobExecutionService.cancelQueuedJob(jobId);

        // Act + Assert
        assertThrows(
                IllegalStateException.class,
                () -> executionService.markRunning(jobId, executionId)
        );

        // Verify it remains cancelled
        Job cancelledJob = jobRepository.findById(jobId)
                .orElseThrow();

        Execution cancelledExecution = executionRepository
                .findById(executionId)
                .orElseThrow();

        assertEquals(JobStatus.CANCELLED, cancelledJob.getStatus());
        assertEquals(
                ExecutionStatus.CANCELLED,
                cancelledExecution.getStatus()
        );
    }

}
