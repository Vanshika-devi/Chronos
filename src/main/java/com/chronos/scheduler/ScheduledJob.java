package com.chronos.scheduler;

public class ScheduledJob {

    private final Long jobId;
    private final Long executionId;
    private final int priority;

    public ScheduledJob(
            Long jobId,
            Long executionId,
            int priority) {

        this.jobId = jobId;
        this.executionId = executionId;
        this.priority = priority;
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
}