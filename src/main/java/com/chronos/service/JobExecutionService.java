package com.chronos.service;

import java.util.List;
import java.util.Comparator;
import java.time.LocalDateTime;

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
import com.chronos.worker.WorkerPool;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class JobExecutionService {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final WorkerPool workerPool;
    private final JobQueue jobQueue;
    private final SequenceGenerator sequenceGenerator;

    public JobExecutionService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            WorkerPool workerPool,
            JobQueue jobQueue,
            SequenceGenerator sequenceGenerator
    ) {
        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.workerPool = workerPool;
        this.jobQueue = jobQueue;
        this.sequenceGenerator = sequenceGenerator;
    }

    /**
     * Queues a job for its first execution.
     */
    @Transactional
    public Execution queueJob(Long jobId) {

        // STEP 1: Lock the job row.
        Job job = jobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        // STEP 2: Validate its current state.
        if (job.getStatus() != JobStatus.CREATED) {

            throw new IllegalStateException(
                    "Only CREATED jobs can be queued. "
                            + "Current status: "
                            + job.getStatus()
            );
        }

        // STEP 3: Move the job to QUEUED.
        job.setStatus(JobStatus.QUEUED);

        jobRepository.save(job);

        // STEP 4: Create its first execution record.
        Execution execution = new Execution();

        execution.setJob(job);
        execution.setStatus(ExecutionStatus.QUEUED);
        execution.setAttemptNumber(1);
        execution.setStartedAt(null);
        execution.setFinishedAt(null);
        execution.setFailureReason(null);

        Execution savedExecution =
                executionRepository.save(execution);

        // STEP 5: Prepare the scheduler entry.
        long sequenceNumber = sequenceGenerator.next();

        ScheduledJob scheduledJob =
                new ScheduledJob(
                        job.getId(),
                        savedExecution.getId(),
                        job.getPriority(),
                        sequenceNumber,
                        "DEFAULT"
                );

        // STEP 6: Add it to the in-memory queue only
        // after the database transaction commits.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        jobQueue.add(scheduledJob);

                        System.out.println(
                                "Job " + job.getId()
                                        + " added to scheduler queue."
                        );
                    }
                }
        );

        return savedExecution;
    }

    /**
     * Dispatches a scheduled job to the worker pool.
     */
    public void dispatchJob(ScheduledJob scheduledJob) {

        Long jobId = scheduledJob.getJobId();
        Long executionId = scheduledJob.getExecutionId();

        jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        executionRepository.findById(executionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Execution not found: " + executionId
                        )
                );

        workerPool.submit(scheduledJob);
    }

    /**
     * Cancels a job that is still waiting in the queue.
     */
    @Transactional
    public void cancelQueuedJob(Long jobId) {

        // Always lock the job first.
        Job job = jobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        if (job.getStatus() != JobStatus.QUEUED) {

            throw new IllegalStateException(
                    "Only queued jobs can be cancelled."
            );
        }

        List<Execution> executions =
                executionRepository.findByJob_Id(jobId);

        Execution queuedExecution = executions.stream()
                .filter(execution ->
                        execution.getStatus()
                                == ExecutionStatus.QUEUED
                )
                .max(Comparator.comparingInt(
                        Execution::getAttemptNumber
                ))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No queued execution found."
                        )
                );

        queuedExecution.setStatus(ExecutionStatus.CANCELLED);
        queuedExecution.setFinishedAt(LocalDateTime.now());

        job.setStatus(JobStatus.CANCELLED);

        executionRepository.save(queuedExecution);
        jobRepository.save(job);

        Long executionId = queuedExecution.getId();

        // Remove from memory only after commit.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        jobQueue.remove(jobId, executionId);
                    }
                }
        );
    }
}
