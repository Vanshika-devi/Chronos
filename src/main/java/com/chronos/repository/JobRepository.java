package com.chronos.repository;

import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository
        extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);
}