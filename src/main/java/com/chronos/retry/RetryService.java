
package com.chronos.retry;

import com.chronos.exception.JobNotFoundException;
import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.scheduler.JobQueue;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.scheduler.SequenceGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Service
public class RetryService {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final JobQueue jobQueue;
    private final SequenceGenerator sequenceGenerator;
    private final RetryPolicy retryPolicy;
    private final ExponentialBackoffPolicy backoffPolicy;

    public RetryService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            JobQueue jobQueue,
            SequenceGenerator sequenceGenerator,
            RetryPolicy retryPolicy,
            ExponentialBackoffPolicy backoffPolicy
    ) {

        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.jobQueue = jobQueue;
        this.sequenceGenerator = sequenceGenerator;
        this.retryPolicy = retryPolicy;
        this.backoffPolicy = backoffPolicy;
    }

    @Transactional
    public boolean scheduleRetry(ScheduledJob failedJob) {

        Long jobId = failedJob.getJobId();
        Long executionId = failedJob.getExecutionId();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        Execution failedExecution =
                executionRepository.findById(executionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Execution not found: "
                                                + executionId
                                )
                        );

        if (!failedExecution.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException(
                    "Execution does not belong to the specified job."
            );
        }

        if (failedExecution.getStatus()
                != ExecutionStatus.FAILED) {

            throw new IllegalStateException(
                    "Only failed executions can be retried."
            );
        }

        int currentAttempt =
                failedExecution.getAttemptNumber();

        int maxAttempts =
                job.getMaxAttempts();

        if (!retryPolicy.shouldRetry(
                currentAttempt,
                maxAttempts
        )) {

            job.setStatus(JobStatus.FAILED);
            jobRepository.save(job);

            System.out.println(
                    "Retry limit reached for Job " + jobId
            );

            return false;
        }

        // Calculate delay for the next attempt.
        Duration retryDelay =
                backoffPolicy.getDelayForAttempt(currentAttempt);

        // Create a completely new execution.
        Execution retryExecution = new Execution();

        retryExecution.setJob(job);
        retryExecution.setStatus(ExecutionStatus.QUEUED);
        retryExecution.setAttemptNumber(currentAttempt + 1);
        retryExecution.setStartedAt(null);
        retryExecution.setFinishedAt(null);
        retryExecution.setFailureReason(null);

        Execution savedExecution =
                executionRepository.save(retryExecution);

        job.setStatus(JobStatus.QUEUED);
        jobRepository.save(job);

        long sequenceNumber =
                sequenceGenerator.next();

        ScheduledJob scheduledRetry =
                new ScheduledJob(
                        job.getId(),
                        savedExecution.getId(),
                        job.getPriority(),
                        sequenceNumber,
                        failedJob.getSchedulingGroup()
                );

        // Queue retry only after the transaction commits.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        jobQueue.addDelayed(
                                scheduledRetry,
                                retryDelay
                        );

                        System.out.println(
                                "Retry scheduled for Job "
                                        + jobId
                                        + " | Attempt: "
                                        + (currentAttempt + 1)
                                        + " | Delay: "
                                        + retryDelay.toSeconds()
                                        + " seconds"
                        );
                    }
                }
        );

        return true;
    }
}
