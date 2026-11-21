
package com.chronos.service;

import com.chronos.dto.CreateJobRequest;
import com.chronos.dto.UpdateJobRequest;
import com.chronos.exception.JobNotFoundException;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(CreateJobRequest request) {

        Job job = new Job(
                null,
                request.getName(),
                request.getPriority(),
                JobStatus.CREATED
        );

        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {

        return jobRepository.findById(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found " + id
                        )
                );
    }

    @Transactional
    public Job updateJob(
            Long id,
            UpdateJobRequest request
    ) {

        // Lock the database row while checking and updating the job.
        Job job = jobRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found " + id
                        )
                );

        // Only CREATED jobs can have their metadata edited.
        if (job.getStatus() != JobStatus.CREATED) {
            throw new IllegalStateException(
                    "Only jobs in CREATED status can be updated. "
                            + "Current status: " + job.getStatus()
            );
        }

        // Update metadata only. Never update status here.
        job.setName(request.getName());
        job.setPriority(request.getPriority());

        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long id) {

        // Lock the row so a concurrent lifecycle operation
        // cannot race with this deletion.
        Job job = jobRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found " + id
                        )
                );

        if (job.getStatus() != JobStatus.CREATED) {
            throw new IllegalStateException(
                    "Only jobs in CREATED status can be deleted. "
                            + "Current status: " + job.getStatus()
            );
        }

        jobRepository.delete(job);
    }
}
