
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
            JobRepository jobRepository
    ) {
        this.executionRepository = executionRepository;
        this.jobRepository = jobRepository;
    }

    public List<Execution> getExecutionsForJob(Long jobId) {
        return executionRepository.findByJob_Id(jobId);
    }

    @Transactional
    public void markRunning(Long jobId, Long executionId) {
        Job job = findJob(jobId);
        Execution execution = findExecution(jobId,executionId);

        if (!execution.getJob().getId().equals(jobId)) {
            throw new IllegalStateException(
                    "Execution does not belong to the specified job"
            );
        }

        if (job.getStatus() != JobStatus.QUEUED
                || execution.getStatus() != ExecutionStatus.QUEUED) {
            throw new IllegalStateException(
                    "Job or execution is no longer queued. "
                            + "Job status: " + job.getStatus()
                            + ", execution status: " + execution.getStatus()
            );
        }

        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());

        job.setStatus(JobStatus.RUNNING);

        executionRepository.save(execution);
        jobRepository.save(job);
    }

    @Transactional
    public void markCompleted(Long jobId, Long executionId) {

        Job job = findJob(jobId);
        Execution execution = findExecution(jobId, executionId);

        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setFinishedAt(LocalDateTime.now());
        execution.setFailureReason(null);

        job.setStatus(JobStatus.COMPLETED);

        executionRepository.save(execution);
        jobRepository.save(job);
    }

    // Backward-compatible method
    @Transactional
    public void markFailed(Long jobId, Long executionId) {

        markFailedInternal(jobId, executionId, null);
    }

    // New method: stores the actual failure reason
    @Transactional
    public void markFailed(
            Long jobId,
            Long executionId,
            String failureReason
    ) {

        markFailedInternal(
                jobId,
                executionId,
                failureReason
        );
    }

    private void markFailedInternal(
            Long jobId,
            Long executionId,
            String failureReason
    ) {

        Job job = findJob(jobId);
        Execution execution = findExecution(jobId, executionId);

        execution.setStatus(ExecutionStatus.FAILED);
        execution.setFinishedAt(LocalDateTime.now());

        execution.setFailureReason(
                failureReason == null
                        ? "Unknown execution failure"
                        : failureReason
        );

        job.setStatus(JobStatus.FAILED);

        executionRepository.save(execution);
        jobRepository.save(job);
    }

    private Job findJob(Long jobId) {

        return jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found: " + jobId
                        )
                );
    }

    private Execution findExecution(
            Long jobId,
            Long executionId
    ) {

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Execution not found: " + executionId
                        )
                );

        if (!execution.getJob().getId().equals(jobId)) {
            throw new IllegalArgumentException(
                    "Execution does not belong to Job " + jobId
            );
        }

        return execution;
    }
    @Transactional
    public void markCancelled(Long jobId, Long executionId) {

        Job job = findJob(jobId);
        Execution execution = findExecution(jobId, executionId);

        if (execution.getStatus() != ExecutionStatus.QUEUED) {
            throw new IllegalStateException(
                    "Only queued executions can be cancelled."
            );
        }

        if (job.getStatus() != JobStatus.QUEUED) {
            throw new IllegalStateException(
                    "Only queued jobs can be cancelled."
            );
        }

        execution.setStatus(ExecutionStatus.CANCELLED);
        execution.setFinishedAt(LocalDateTime.now());

        job.setStatus(JobStatus.CANCELLED);

        executionRepository.save(execution);
        jobRepository.save(job);
    }
}
