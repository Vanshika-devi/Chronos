package com.chronos.service;

import com.chronos.exception.JobNotFoundException;
import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final JobRepository jobRepository;

    public ExecutionService(
            ExecutionRepository executionRepository,
            JobRepository jobRepository) {

        this.executionRepository = executionRepository;
        this.jobRepository = jobRepository;
    }

    public List<Execution> getExecutionsForJob(
            Long jobId) {

        return executionRepository.findByJobId(jobId);
    }

    @Transactional
    public void markRunning(
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

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        execution.setStartedAt(
                LocalDateTime.now()
        );

        job.setStatus(
                JobStatus.RUNNING
        );

        executionRepository.save(execution);
        jobRepository.save(job);
    }

    @Transactional
    public void markCompleted(
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

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        execution.setFinishedAt(
                LocalDateTime.now()
        );

        job.setStatus(
                JobStatus.COMPLETED
        );

        executionRepository.save(execution);
        jobRepository.save(job);
    }

    @Transactional
    public void markFailed(
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

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        execution.setFinishedAt(
                LocalDateTime.now()
        );

        job.setStatus(
                JobStatus.FAILED
        );

        executionRepository.save(execution);
        jobRepository.save(job);
    }
}