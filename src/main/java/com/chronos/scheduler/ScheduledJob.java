package com.chronos.scheduler;

public class ScheduledJob {

    private final Long jobId;
    private final Long executionId;
    private final int priority;
    private final long sequenceNumber;

    public ScheduledJob(
            Long jobId,
            Long executionId,
            int priority,
            long sequenceNumber) {

        this.jobId = jobId;
        this.executionId = executionId;
        this.priority = priority;
        this.sequenceNumber = sequenceNumber;
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
}