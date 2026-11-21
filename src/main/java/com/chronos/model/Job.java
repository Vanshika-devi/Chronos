package com.chronos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int priority;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(nullable = false)
    private int maxAttempts = 3;

    public Job() {
    }

    // Backward-compatible constructor
    public Job(
            Long id,
            String name,
            int priority,
            JobStatus status
    ) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.status = status;
        this.maxAttempts = 3;
    }

    public Job(
            Long id,
            String name,
            int priority,
            JobStatus status,
            int maxAttempts
    ) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.status = status;
        setMaxAttempts(maxAttempts);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public JobStatus getStatus() {
        return status;
    }

    /**
     * The only approved way to change job status.
     */
    public void transitionTo(JobStatus nextStatus) {

        if (status == null ||
                !status.canTransitionTo(nextStatus)) {

            throw new IllegalStateException(
                    "Invalid job status transition: "
                            + status + " -> " + nextStatus
            );
        }

        this.status = nextStatus;
    }

    /**
     * Keep this setter for JavaBean compatibility,
     * but do not allow it to bypass lifecycle rules.
     */
    public void setStatus(JobStatus status) {
        transitionTo(status);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "Maximum attempts must be at least 1"
            );
        }

        this.maxAttempts = maxAttempts;
    }
}