package com.chronos.repository;

import com.chronos.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExecutionRepository
        extends JpaRepository<Execution, Long> {

    List<Execution> findByJob_Id(Long jobId);

    Optional<Execution> findByJob_IdAndIdempotencyKey(
            Long jobId,
            String idempotencyKey
    );
}
