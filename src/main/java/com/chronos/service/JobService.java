package com.chronos.service;

import com.chronos.dto.CreateJobRequest;
import com.chronos.dto.UpdateJobRequest;
import com.chronos.repository.JobRepository;
import com.chronos.model.Job;
import org.springframework.stereotype.Service;
import com.chronos.exception.JobNotFoundException;

import java.util.List;

@Service
public class JobService{
    private final JobRepository jobRepository;
    public JobService(JobRepository jobRepository){
        this.jobRepository=jobRepository;
    }
    public Job createJob(CreateJobRequest request){
        Job job=new Job(
                null,
                request.getName(),
                request.getPriority(),
                "CREATED"
        );
        return jobRepository.save(job);
    }
    public List<Job> getAllJobs(){
        return jobRepository.findAll();
    }
    public Job getJobById(Long id){
        return jobRepository.findById(id)
                .orElseThrow(()->new JobNotFoundException("Job not found "+id)
                );
    }
    public Job updateJob(Long id, UpdateJobRequest request){
        Job job=jobRepository.findById(id)
                .orElseThrow(()->
                        new JobNotFoundException(
                                "Job "+ id
                        )
                );
        job.setName(request.getName());
        job.setPriority(request.getPriority());
        job.setStatus(request.getStatus());
        return jobRepository.save(job);
    }
    public Job updateJobStatus(Long id,String status){
        Job job=jobRepository.findById(id)
                .orElseThrow(()->
                        new JobNotFoundException(
                                "Job not found "+ id
                        ));
        job.setStatus(status);
        return jobRepository.save(job);
    }

    public void deleteJob(Long id){

        if(!jobRepository.existsById(id)){
            throw new JobNotFoundException(
                    "Job not found "+ id
            );
        }
        jobRepository.deleteById(id);
    }
}