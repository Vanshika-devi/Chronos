package com.chronos.scheduler;

import java.time.Instant;

public class ScheduledJob {

    private final Long jobId;
    private final Long executionId;
    private final int priority;
    private final long sequenceNumber;
    private final String schedulingGroup;

    private Instant queuedAt;

    public ScheduledJob(
            Long jobId,
            Long executionId,
            int priority,
            long sequenceNumber,
            String schedulingGroup) {

        this.jobId = jobId;
        this.executionId = executionId;
        this.priority = priority;
        this.sequenceNumber = sequenceNumber;
        this.schedulingGroup = schedulingGroup;
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public int getPriority() {
        return priority;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getSchedulingGroup() {
        return schedulingGroup;
    }

    public Instant getQueuedAt() {
        return queuedAt;
    }

    public void markQueued(Instant queuedAt) {

        if (queuedAt == null) {
            throw new IllegalArgumentException(
                    "queuedAt cannot be null."
            );
        }

        this.queuedAt = queuedAt;
    }
}