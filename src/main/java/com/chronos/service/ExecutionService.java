package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import org.springframework.stereotype.Service;

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

    public List<Execution> getExecutionsForJob(Long jobId) {

        return executionRepository.findByJobId(jobId);
    }

    public void markCompleted(
            Execution execution,
            Job job) {

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        execution.setFinishedAt(
                LocalDateTime.now()
        );

        job.setStatus(JobStatus.COMPLETED);

        executionRepository.save(execution);
        jobRepository.save(job);
    }

    public void markFailed(
            Execution execution,
            Job job) {

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        execution.setFinishedAt(
                LocalDateTime.now()
        );

        job.setStatus(JobStatus.FAILED);

        executionRepository.save(execution);
        jobRepository.save(job);
    }
}