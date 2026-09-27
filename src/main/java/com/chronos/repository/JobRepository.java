package com.chronos.repository;

import com.chronos.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job,Long>{

}