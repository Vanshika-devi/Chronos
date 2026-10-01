package com.chronos.service;

import com.chronos.model.*;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.exception.JobNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobExecutionService{
    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;

    public JobExecutionService(JobRepository jobRepository,ExecutionRepository executionRepository){
        this.jobRepository=jobRepository;
        this.executionRepository=executionRepository;
    }

    @Transactional
    public Execution runJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(
                                "Job not found : " + jobId
                        )
                );
        Execution execution = new Execution();
        execution.setJobId(job.getId());
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());

        job.setStatus(JobStatus.RUNNING);
        jobRepository.save(job);
        try{

            //Temporary simualtion of the job work
            Thread.sleep(2000);

            execution.setStatus(
                    ExecutionStatus.COMPLETED
            );
            execution.setFinishedAt(
                    LocalDateTime.now()
            );
            job.setStatus(
                    JobStatus.COMPLETED
            );
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
            execution.setStatus(
                    ExecutionStatus.FAILED
            );
            execution.setFinishedAt(
                    LocalDateTime.now()
            );
            job.setStatus(JobStatus.FAILED);
        }
        jobRepository.save(job);
        return executionRepository.save(execution);
    }
}