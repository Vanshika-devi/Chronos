package com.chronos.repository;

import com.chronos.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionRepository
        extends JpaRepository<Execution, Long> {

    List<Execution> findByJobId(Long jobId);
}