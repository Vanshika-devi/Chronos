package com.chronos.service;
import java.util.List;
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

import java.time.LocalDateTime;
import java.util.Comparator;
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

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        job.setStatus(JobStatus.QUEUED);

        jobRepository.save(job);

        Execution execution = new Execution();

        execution.setJob(job);
        execution.setStatus(
                ExecutionStatus.QUEUED
        );
        execution.setStartedAt(null);
        execution.setFinishedAt(null);

        Execution savedExecution =
                executionRepository.save(execution);

        long sequenceNumber =
                sequenceGenerator.next();

        ScheduledJob scheduledJob =
                new ScheduledJob(
                        job.getId(),
                        savedExecution.getId(),
                        job.getPriority(),
                        sequenceNumber,
                        "DEFAULT"
                );

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                jobQueue.add(
                                        scheduledJob
                                );

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
                        new RuntimeException(
                                "Execution not found: "
                                        + executionId
                        )
                );

        workerPool.submit(scheduledJob);
    }

    @Transactional
    public void cancelQueuedJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException("Job not found: " + jobId)
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
                        execution.getStatus() == ExecutionStatus.QUEUED)
                .max(Comparator.comparingInt(Execution::getAttemptNumber))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No queued execution found for this job."
                        )
                );

        queuedExecution.setStatus(ExecutionStatus.CANCELLED);
        queuedExecution.setFinishedAt(LocalDateTime.now());

        job.setStatus(JobStatus.CANCELLED);

        executionRepository.save(queuedExecution);
        jobRepository.save(job);

        Long executionId = queuedExecution.getId();

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