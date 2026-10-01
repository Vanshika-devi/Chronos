package com.chronos.controller;

import com.chronos.dto.CreateJobRequest;
import com.chronos.dto.UpdateJobRequest;
import com.chronos.dto.UpdateJobStatusRequest;
import com.chronos.model.Execution;
import com.chronos.model.Job;
import com.chronos.service.ExecutionService;
import com.chronos.service.JobExecutionService;
import com.chronos.service.JobService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/jobs")
public class JobController{
    private final JobService jobService;
    private final JobExecutionService jobExecutionService;
    private final ExecutionService executionService;

    public JobController(JobService jobService, JobExecutionService jobExecutionService, ExecutionService executionService){
        this.jobService=jobService;
        this.jobExecutionService = jobExecutionService;
        this.executionService = executionService;
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@Valid @RequestBody CreateJobRequest request){
        Job job=jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs(){
        return ResponseEntity.ok(
                jobService.getAllJobs()
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id){
        return ResponseEntity.ok(
                jobService.getJobById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id,@Valid @RequestBody UpdateJobRequest request){
        return ResponseEntity.ok(
                jobService.updateJob(id,request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Job> updateJobStatus(@PathVariable Long id, @Valid @RequestBody UpdateJobStatusRequest request){
        return ResponseEntity.ok(
                jobService.updateJobStatus(
                        id,
                        request.getStatus()
                )
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id){
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Execution> runJob(@PathVariable Long id){
        return ResponseEntity.ok(
                jobExecutionService.runJob(id)
        );
    }
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<Execution>> getExecutions(@PathVariable Long id){
        return ResponseEntity.ok(
                executionService.getExecutionsForJob(id)
        );
    }
}