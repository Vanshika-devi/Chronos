
package com.chronos.service;

import com.chronos.dto.CreateJobRequest;
import com.chronos.dto.UpdateJobRequest;
import com.chronos.exception.JobNotFoundException;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository);
    }

    @Test
    void createJobShouldSaveJobWithCreatedStatus() {
        CreateJobRequest request = new CreateJobRequest();
        request.setName("Email Job");
        request.setPriority(5);

        Job savedJob =
                new Job(1L, "Email Job", 5, JobStatus.CREATED);

        when(jobRepository.save(any(Job.class)))
                .thenReturn(savedJob);

        Job result = jobService.createJob(request);

        assertEquals(1L, result.getId());
        assertEquals(JobStatus.CREATED, result.getStatus());

        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void getJobByIdShouldReturnJobWhenItExists() {
        Job job = new Job(1L, "Email Job", 5, JobStatus.CREATED);

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        Job result = jobService.getJobById(1L);

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

    @Test
    void updateJobChangesMetadataButPreservesStatus() {
        Job job = new Job(1L, "Before", 2, JobStatus.CREATED);

        when(jobRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(job));

        when(jobRepository.save(job)).thenReturn(job);

        UpdateJobRequest request = new UpdateJobRequest();
        request.setName("After");
        request.setPriority(8);

        Job updated = jobService.updateJob(1L, request);

        assertEquals("After", updated.getName());
        assertEquals(8, updated.getPriority());
        assertEquals(JobStatus.CREATED, updated.getStatus());

        verify(jobRepository).findByIdForUpdate(1L);
    }

    @Test
    void updateJobRejectsQueuedJobs() {
        Job job = new Job(1L, "Queued", 2, JobStatus.QUEUED);

        when(jobRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(job));

        UpdateJobRequest request = new UpdateJobRequest();
        request.setName("Changed");
        request.setPriority(9);

        assertThrows(
                IllegalStateException.class,
                () -> jobService.updateJob(1L, request)
        );

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void deleteJobRejectsQueuedJobs() {
        Job job = new Job(1L, "Queued", 1, JobStatus.QUEUED);

        when(jobRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(job));

        assertThrows(
                IllegalStateException.class,
                () -> jobService.deleteJob(1L)
        );

        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    void deleteJobAllowsCreatedJobs() {
        Job job = new Job(
                1L,
                "Not yet queued",
                1,
                JobStatus.CREATED
        );

        when(jobRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(job));

        jobService.deleteJob(1L);

        verify(jobRepository).delete(job);
    }
}
