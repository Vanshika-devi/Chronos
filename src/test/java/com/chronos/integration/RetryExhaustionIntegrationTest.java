package com.chronos.integration;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.scheduler.JobExecutor;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.service.JobExecutionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RetryExhaustionIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private JobExecutionService jobExecutionService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public AlwaysFailingJobExecutor alwaysFailingJobExecutor() {
            return new AlwaysFailingJobExecutor();
        }
    }

    public static class AlwaysFailingJobExecutor
            implements JobExecutor {

        @Override
        public void execute(ScheduledJob job) throws Exception {

            System.out.println(
                    "Test executor: Job " + job.getJobId()
                            + " | Execution " + job.getExecutionId()
                            + " | Simulating permanent failure"
            );

            throw new Exception("Simulated permanent failure");
        }
    }

    @Test
    void shouldStopRetryingWhenMaximumAttemptsReached()
            throws Exception {

        // 1. Create a job with a maximum of three attempts.
        Job job = new Job(
                null,
                "retry-exhaustion-test",
                5,
                JobStatus.CREATED,
                3
        );

        job = jobRepository.saveAndFlush(job);

        Long jobId = job.getId();

        // 2. Queue the job through the actual service.
        jobExecutionService.queueJob(jobId);

        // 3. Wait for all retry attempts to finish.
        long deadline = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(30);

        boolean completed = false;

        while (System.nanoTime() < deadline) {

            List<Execution> currentExecutions =
                    executionRepository.findByJob_Id(jobId);

            boolean thirdAttemptFailed =
                    currentExecutions.stream().anyMatch(execution ->
                            execution.getAttemptNumber() == 3
                                    && execution.getStatus()
                                    == ExecutionStatus.FAILED
                    );

            Job currentJob =
                    jobRepository.findById(jobId).orElseThrow();

            if (thirdAttemptFailed
                    && currentJob.getStatus() == JobStatus.FAILED) {

                completed = true;
                break;
            }

            Thread.sleep(200);
        }

        // 4. Verify that retry exhaustion completed.
        assertTrue(
                completed,
                "Job did not reach FAILED status within 30 seconds"
        );

        // 5. Retrieve and sort all executions.
        List<Execution> executions = new ArrayList<>(
                executionRepository.findByJob_Id(jobId)
        );

        executions.sort(
                Comparator.comparingInt(Execution::getAttemptNumber)
        );

        // 6. Verify exactly three attempts.
        assertEquals(
                3,
                executions.size(),
                "No fourth execution should be created"
        );

        assertEquals(1, executions.get(0).getAttemptNumber());
        assertEquals(2, executions.get(1).getAttemptNumber());
        assertEquals(3, executions.get(2).getAttemptNumber());

        // 7. Every attempt must have failed.
        assertTrue(
                executions.stream().allMatch(
                        execution ->
                                execution.getStatus() == ExecutionStatus.FAILED
                )
        );

        // 8. Verify the failure reason.
        assertEquals(
                "Simulated permanent failure",
                executions.get(2).getFailureReason()
        );

        // 9. Verify the final job status.
        Job finalJob =
                jobRepository.findById(jobId).orElseThrow();

        assertEquals(JobStatus.FAILED, finalJob.getStatus());

        // 10. Print the execution history.
        System.out.println("\n========================================");
        System.out.println("     RETRY EXHAUSTION INTEGRATION TEST");
        System.out.println("========================================");
        System.out.println("Job ID        : " + jobId);
        System.out.println("Attempt 1     : " + executions.get(0).getStatus());
        System.out.println("Attempt 2     : " + executions.get(1).getStatus());
        System.out.println("Attempt 3     : " + executions.get(2).getStatus());
        System.out.println("Total Attempts: " + executions.size());
        System.out.println("Final Status  : " + finalJob.getStatus());
        System.out.println("========================================\n");
    }
}
