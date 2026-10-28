package com.chronos.service;

import com.chronos.dto.CreateJobRequest;
import com.chronos.dto.UpdateJobRequest;
import com.chronos.exception.JobNotFoundException;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository, ExecutionRepository executionRepository) {
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

    public Job updateJob(
            Long id,
            UpdateJobRequest request
    ) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found " + id
                        )
                );

        job.setName(request.getName());
        job.setPriority(request.getPriority());
        job.setStatus(request.getStatus());

        return jobRepository.save(job);
    }

    public Job updateJobStatus(
            Long id,
            JobStatus status
    ) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new JobNotFoundException(
                                "Job not found " + id
                        )
                );

        job.setStatus(status);

        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {
            throw new JobNotFoundException(
                    "Job not found " + id
            );
        }

        jobRepository.deleteById(id);
    }
}