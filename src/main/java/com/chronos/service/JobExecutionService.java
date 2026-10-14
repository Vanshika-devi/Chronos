package com.chronos.service;

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
            SequenceGenerator sequenceGenerator) {

        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.workerPool = workerPool;
        this.jobQueue = jobQueue;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional
    public Execution queueJob(Long jobId) {

        /*
         * Find the job in the database.
         */
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        /*
         * Mark the job as QUEUED.
         */
        job.setStatus(JobStatus.QUEUED);

        jobRepository.save(job);

        /*
         * Create an execution record.
         */
        Execution execution = new Execution();

        execution.setJobId(job.getId());
        execution.setStatus(ExecutionStatus.QUEUED);
        execution.setStartedAt(null);

        Execution savedExecution =
                executionRepository.save(execution);

        /*
         * Generate the ordering number.
         *
         * This is used when two jobs have
         * the same effective priority.
         */
        long sequenceNumber =
                sequenceGenerator.next();

        /*
         * Create the object that will enter
         * the scheduler queue.
         */
        ScheduledJob scheduledJob =
                new ScheduledJob(
                        job.getId(),
                        savedExecution.getId(),
                        job.getPriority(),
                        sequenceNumber,
                        "DEFAULT"
                );

        /*
         * IMPORTANT:
         *
         * Do not put the job into the queue until
         * the database transaction has committed.
         *
         * Otherwise the scheduler could consume the
         * job before the Execution row exists in the DB.
         */
        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                jobQueue.add(scheduledJob);

                                System.out.println(
                                        "Job "
                                                + job.getId()
                                                + " added to scheduler queue "
                                                + "after transaction commit. "
                                                + "Priority: "
                                                + job.getPriority()
                                                + ", Sequence: "
                                                + sequenceNumber
                                );
                            }
                        }
                );

        return savedExecution;
    }

    public void dispatchJob(
            Long jobId,
            Long executionId) {

        /*
         * Verify that the job exists.
         */
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        /*
         * Verify that the execution exists.
         */
        Execution execution =
                executionRepository.findById(executionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Execution not found: "
                                                + executionId
                                )
                        );

        /*
         * Verify that the execution actually
         * belongs to this job.
         */
        if (!job.getId().equals(execution.getJobId())) {

            throw new IllegalStateException(
                    "Execution " + executionId
                            + " does not belong to job "
                            + jobId
            );
        }

        /*
         * Only dispatch a QUEUED execution.
         */
        if (execution.getStatus()
                != ExecutionStatus.QUEUED) {

            throw new IllegalStateException(
                    "Execution " + executionId
                            + " is not QUEUED. Current status: "
                            + execution.getStatus()
            );
        }

        /*
         * Submit to worker pool.
         */
        workerPool.submit(
                job.getId(),
                execution.getId()
        );
    }
}