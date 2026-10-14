package com.chronos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "executions")
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    // Retry metadata
    private int attemptNumber;

    private int maxRetries;

    private String failureReason;

    public Execution() {
    }

    public Execution(
            Long id,
            Long jobId,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt) {

        this.id = id;
        this.jobId = jobId;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}