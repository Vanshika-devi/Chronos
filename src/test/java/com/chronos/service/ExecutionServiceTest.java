package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.exception.ExecutionNotFoundException;
import com.chronos.repository.JobRepository;
import com.chronos.exception.JobNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ExecutionService executionService;

    @Test
    void shouldMarkExecutionRunning() {

        Long jobId = 1L;
        Long executionId = 10L;

        Job job = new Job(
                jobId,
                "test-job",
                5,
                JobStatus.QUEUED
        );

        Execution execution = new Execution(
                executionId,
                jobId,
                ExecutionStatus.QUEUED,
                null,
                null
        );

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(executionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        executionService.markRunning(
                jobId,
                executionId
        );

        assertEquals(
                ExecutionStatus.RUNNING,
                execution.getStatus()
        );

        assertEquals(
                JobStatus.RUNNING,
                job.getStatus()
        );

        assertNotNull(execution.getStartedAt());

        verify(executionRepository)
                .save(execution);

        verify(jobRepository)
                .save(job);
    }

    @Test
    void shouldMarkExecutionCompleted() {

        Long jobId = 1L;
        Long executionId = 10L;

        Job job = new Job(
                jobId,
                "test-job",
                5,
                JobStatus.RUNNING
        );

        Execution execution = new Execution(
                executionId,
                jobId,
                ExecutionStatus.RUNNING,
                null,
                null
        );

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(executionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        executionService.markCompleted(
                jobId,
                executionId
        );

        assertEquals(
                ExecutionStatus.COMPLETED,
                execution.getStatus()
        );

        assertEquals(
                JobStatus.COMPLETED,
                job.getStatus()
        );

        assertNotNull(execution.getFinishedAt());

        verify(executionRepository)
                .save(execution);

        verify(jobRepository)
                .save(job);
    }

    @Test
    void shouldMarkExecutionFailed() {

        Long jobId = 1L;
        Long executionId = 10L;

        Job job = new Job(
                jobId,
                "test-job",
                5,
                JobStatus.RUNNING
        );

        Execution execution = new Execution(
                executionId,
                jobId,
                ExecutionStatus.RUNNING,
                null,
                null
        );

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(executionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        executionService.markFailed(
                jobId,
                executionId
        );

        assertEquals(
                ExecutionStatus.FAILED,
                execution.getStatus()
        );

        assertEquals(
                JobStatus.FAILED,
                job.getStatus()
        );

        assertNotNull(execution.getFinishedAt());

        verify(executionRepository)
                .save(execution);

        verify(jobRepository)
                .save(job);
    }

    @Test
    void shouldThrowWhenExecutionDoesNotExist() {

        Long jobId = 1L;
        Long executionId = 10L;

        Job job = new Job(
                jobId,
                "test-job",
                5,
                JobStatus.QUEUED
        );

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(executionRepository.findById(executionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExecutionNotFoundException.class,
                () -> executionService.markRunning(
                        jobId,
                        executionId
                )
        );

        verify(executionRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenJobDoesNotExist() {

        Long jobId = 999L;
        Long executionId = 10L;

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.empty());

        assertThrows(
                JobNotFoundException.class,
                () -> executionService.markRunning(
                        jobId,
                        executionId
                )
        );

        verify(executionRepository, never())
                .save(any());
    }
}