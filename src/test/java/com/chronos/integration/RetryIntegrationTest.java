
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

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RetryIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private ControlledJobExecutor controlledJobExecutor;

    @Test
    void shouldRetryFailedJobAndCompleteSuccessfully()
            throws Exception {

        // 1. Create job with maximum 3 attempts.

        Job job = new Job(
                null,
                "retry-integration-test",
                5,
                JobStatus.CREATED,
                3
        );

        job = jobRepository.save(job);

        controlledJobExecutor.failFirstAttemptFor(job.getId());

        // 2. Queue the initial execution.

        Execution firstExecution =
                jobExecutionService.queueJob(job.getId());

        assertNotNull(firstExecution.getId());

        // 3. Wait for retry execution to complete.

        List<Execution> executions = List.of();

        long deadline =
                System.nanoTime()
                        + TimeUnit.SECONDS.toNanos(15);

        while (System.nanoTime() < deadline) {

            executions =
                    executionRepository.findByJob_Id(job.getId());

            boolean retryCompleted = executions.stream()
                    .anyMatch(execution ->
                            execution.getAttemptNumber() == 2
                                    && execution.getStatus()
                                    == ExecutionStatus.COMPLETED
                    );

            if (retryCompleted) {
                break;
            }

            Thread.sleep(100);
        }

        // 4. Sort execution history by attempt number.

        executions = executionRepository.findByJob_Id(job.getId())
                .stream()
                .sorted(
                        Comparator.comparingInt(
                                Execution::getAttemptNumber
                        )
                )
                .toList();

        // 5. Verify execution history.

        assertEquals(
                2,
                executions.size(),
                "Expected exactly two execution records"
        );

        Execution firstAttempt = executions.get(0);
        Execution secondAttempt = executions.get(1);

        assertEquals(1, firstAttempt.getAttemptNumber());
        assertEquals(
                ExecutionStatus.FAILED,
                firstAttempt.getStatus()
        );

        assertEquals(
                "Simulated failure on first attempt",
                firstAttempt.getFailureReason()
        );

        assertEquals(2, secondAttempt.getAttemptNumber());
        assertEquals(
                ExecutionStatus.COMPLETED,
                secondAttempt.getStatus()
        );

        // 6. Verify final job status.

        Job finalJob =
                jobRepository.findById(job.getId())
                        .orElseThrow();

        assertEquals(
                JobStatus.COMPLETED,
                finalJob.getStatus()
        );

        assertEquals(
                2,
                controlledJobExecutor.getAttemptCount(job.getId())
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println("       RETRY INTEGRATION TEST");
        System.out.println("========================================");

        System.out.println(
                "Job ID       : " + job.getId()
        );

        System.out.println(
                "Attempt 1    : " + firstAttempt.getStatus()
        );

        System.out.println(
                "Attempt 2    : " + secondAttempt.getStatus()
        );

        System.out.println(
                "Final Status : " + finalJob.getStatus()
        );

        System.out.println(
                "Total Attempts: " + executions.size()
        );

        System.out.println("========================================");
    }

    @TestConfiguration
    static class RetryTestConfiguration {

        @Bean
        @Primary
        ControlledJobExecutor controlledJobExecutor() {
            return new ControlledJobExecutor();
        }
    }

    static class ControlledJobExecutor implements JobExecutor {

        private final AtomicReference<Long> failingJobId =
                new AtomicReference<>();

        private final ConcurrentHashMap<Long, AtomicInteger>
                attempts = new ConcurrentHashMap<>();

        void failFirstAttemptFor(Long jobId) {
            failingJobId.set(jobId);
        }

        @Override
        public void execute(ScheduledJob job) throws Exception {

            Long jobId = job.getJobId();

            if (!jobId.equals(failingJobId.get())) {
                return;
            }

            int attempt = attempts
                    .computeIfAbsent(
                            jobId,
                            id -> new AtomicInteger(0)
                    )
                    .incrementAndGet();

            System.out.println(
                    "Test executor: Job "
                            + jobId
                            + " | Attempt "
                            + attempt
            );

            if (attempt == 1) {
                throw new Exception(
                        "Simulated failure on first attempt"
                );
            }

            System.out.println(
                    "Test executor: Job "
                            + jobId
                            + " succeeded on retry."
            );
        }

        int getAttemptCount(Long jobId) {

            AtomicInteger counter = attempts.get(jobId);

            return counter == null
                    ? 0
                    : counter.get();
        }
    }
}
