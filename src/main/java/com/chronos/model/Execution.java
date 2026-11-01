
package com.chronos.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "executions")
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(nullable = false)
    private int attemptNumber = 1;

    @Column(length = 2000)
    private String failureReason;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    public Execution() {
    }

    // Backward-compatible constructor
    public Execution(
            Long id,
            Job job,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        this.id = id;
        this.job = job;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.attemptNumber = 1;
    }

    public Execution(
            Long id,
            Job job,
            ExecutionStatus status,
            int attemptNumber,
            String failureReason,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        this.id = id;
        this.job = job;
        this.status = status;
        setAttemptNumber(attemptNumber);
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        if (attemptNumber < 1) {
            throw new IllegalArgumentException(
                    "Attempt number must be at least 1"
            );
        }

        this.attemptNumber = attemptNumber;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
