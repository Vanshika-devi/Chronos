package com.chronos.service;

import com.chronos.dto.CreateJobRequest;
import com.chronos.dto.UpdateJobRequest;
import com.chronos.exception.JobNotFoundException;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ExecutionRepository executionRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(
                jobRepository,
                executionRepository
        );
    }

    @Test
    void createJobShouldSaveJobWithCreatedStatus() {

        CreateJobRequest request = new CreateJobRequest();

        request.setName("Email Job");
        request.setPriority(5);

        Job savedJob = new Job(
                1L,
                "Email Job",
                5,
                JobStatus.CREATED
        );

        when(jobRepository.save(any(Job.class)))
                .thenReturn(savedJob);

        Job result = jobService.createJob(request);

        assertEquals(1L, result.getId());
        assertEquals("Email Job", result.getName());
        assertEquals(5, result.getPriority());
        assertEquals(JobStatus.CREATED, result.getStatus());

        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void getJobByIdShouldReturnJobWhenItExists() {

        Job job = new Job(
                1L,
                "Email Job",
                5,
                JobStatus.CREATED
        );

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        Job result = jobService.getJobById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Email Job", result.getName());

        verify(jobRepository).findById(1L);
    }

    @Test
    void getJobByIdShouldThrowWhenJobDoesNotExist() {

        when(jobRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                JobNotFoundException.class,
                () -> jobService.getJobById(99L)
        );

        verify(jobRepository).findById(99L);
    }
}