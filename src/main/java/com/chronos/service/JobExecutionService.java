package com.chronos.service;

import com.chronos.exception.JobNotFoundException;
import com.chronos.model.*;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.worker.WorkerPool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobExecutionService {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final WorkerPool workerPool;

    public JobExecutionService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            WorkerPool workerPool) {

        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.workerPool = workerPool;
    }

    @Transactional
    public Execution runJob(Long jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );

        Execution execution = new Execution();

        execution.setJobId(job.getId());

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        execution.setStartedAt(
                LocalDateTime.now()
        );

        job.setStatus(JobStatus.RUNNING);

        jobRepository.save(job);

        Execution savedExecution =
                executionRepository.save(execution);

        workerPool.submit(
                job,
                savedExecution
        );

        return savedExecution;
    }
}