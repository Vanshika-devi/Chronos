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
import com.chronos.worker.WorkerPool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobExecutionService {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final WorkerPool workerPool;
    private final JobQueue jobQueue;

    public JobExecutionService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            WorkerPool workerPool,
            JobQueue jobQueue) {

        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.workerPool = workerPool;
        this.jobQueue = jobQueue;
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

        execution.setJobId(job.getId());
        execution.setStatus(ExecutionStatus.QUEUED);
        execution.setStartedAt(null);

        Execution savedExecution =
                executionRepository.save(execution);

        ScheduledJob scheduledJob =
                new ScheduledJob(
                        job.getId(),
                        savedExecution.getId(),
                        job.getPriority()
                );

        jobQueue.add(scheduledJob);

        System.out.println(
                "Job " + job.getId()
                        + " added to scheduler queue."
        );

        return savedExecution;
    }

    public void dispatchJob(
            Long jobId,
            Long executionId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        Execution execution =
                executionRepository.findById(executionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Execution not found: "
                                                + executionId
                                )
                        );

        workerPool.submit(
                job.getId(),
                execution.getId()
        );
    }
}