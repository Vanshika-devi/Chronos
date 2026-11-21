package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.scheduler.SchedulerService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobConcurrencyIntegrationTest {

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockBean
    private SchedulerService schedulerService;

    @Test
    void cancellationAndWorkerStartMustNotBothSucceed()
            throws Exception {

        Job job = new Job(
                null,
                "Concurrency Test",
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

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> cancellation = () -> {
            ready.countDown();
            start.await();

            try {
                transactionTemplate.executeWithoutResult(status ->
                        jobExecutionService.cancelQueuedJob(jobId)
                );
                return true;
            } catch (IllegalStateException ex) {
                return false;
            }
        };

        Callable<Boolean> workerStart = () -> {
            ready.countDown();
            start.await();

            try {
                transactionTemplate.executeWithoutResult(status ->
                        executionService.markRunning(
                                jobId,
                                executionId
                        )
                );
                return true;
            } catch (IllegalStateException ex) {
                return false;
            }
        };

        try {
            Future<Boolean> cancelFuture =
                    executor.submit(cancellation);

            Future<Boolean> startFuture =
                    executor.submit(workerStart);

            assertTrue(ready.await(5, TimeUnit.SECONDS));

            start.countDown();

            boolean cancellationSucceeded =
                    cancelFuture.get(15, TimeUnit.SECONDS);

            boolean workerStarted =
                    startFuture.get(15, TimeUnit.SECONDS);

            // Exactly one operation must win.
            assertNotEquals(
                    cancellationSucceeded,
                    workerStarted,
                    "Exactly one lifecycle transition must succeed."
            );

            Job finalJob = jobRepository.findById(jobId)
                    .orElseThrow();

            Execution finalExecution =
                    executionRepository.findById(executionId)
                            .orElseThrow();

            if (cancellationSucceeded) {
                assertEquals(JobStatus.CANCELLED,
                        finalJob.getStatus());

                assertEquals(ExecutionStatus.CANCELLED,
                        finalExecution.getStatus());
            } else {
                assertEquals(JobStatus.RUNNING,
                        finalJob.getStatus());

                assertEquals(ExecutionStatus.RUNNING,
                        finalExecution.getStatus());
            }

        } finally {
            executor.shutdownNow();
        }
    }
}
