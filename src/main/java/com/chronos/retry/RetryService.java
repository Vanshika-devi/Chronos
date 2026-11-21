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

        // Lock the job row to prevent concurrent retry decisions.
        Job job = jobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        Execution failedExecution =
                executionRepository.findById(executionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Execution not found: " + executionId
                                )
                        );

        // Ensure the execution belongs to this job.
        if (!failedExecution.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException(
                    "Execution does not belong to the specified job."
            );
        }

        // Only failed executions can be retried.
        if (failedExecution.getStatus() != ExecutionStatus.FAILED) {
            throw new IllegalStateException(
                    "Only failed executions can be retried."
            );
        }

        // The job must also be in FAILED state.
        if (job.getStatus() != JobStatus.FAILED) {
            throw new IllegalStateException(
                    "Only failed jobs can be retried. Current status: "
                            + job.getStatus()
            );
        }

        int currentAttempt = failedExecution.getAttemptNumber();
        int maxAttempts = job.getMaxAttempts();

        // Stop when the maximum number of attempts is reached.
        if (!retryPolicy.shouldRetry(currentAttempt, maxAttempts)) {

            // ExecutionService has already marked the job as FAILED.
            // Do not attempt the invalid FAILED -> FAILED transition.

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

        // Transition the job from FAILED back to QUEUED.
        job.transitionTo(JobStatus.QUEUED);
        jobRepository.save(job);

        long sequenceNumber = sequenceGenerator.next();

        ScheduledJob scheduledRetry =
                new ScheduledJob(
                        job.getId(),
                        savedExecution.getId(),
                        job.getPriority(),
                        sequenceNumber,
                        failedJob.getSchedulingGroup()
                );

        // Add the retry to the in-memory queue only after the
        // database transaction successfully commits.
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
