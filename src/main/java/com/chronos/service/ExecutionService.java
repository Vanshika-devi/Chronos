package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.repository.ExecutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionService{

    private final ExecutionRepository executionRepository;

    public ExecutionService(ExecutionRepository executionRepository){
        this.executionRepository=executionRepository;
    }

    public List<Execution> getExecutionsForJob(Long jobId){
        return executionRepository.findByJobId(jobId);
    }
}