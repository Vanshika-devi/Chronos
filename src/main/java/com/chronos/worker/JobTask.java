package com.chronos.worker;

import com.chronos.scheduler.JobExecutor;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.service.ExecutionService;

public class JobTask implements Runnable {

    private final ScheduledJob scheduledJob;
    private final ExecutionService executionService;
    private final JobExecutor jobExecutor;

    public JobTask(
            ScheduledJob scheduledJob,
            ExecutionService executionService,
            JobExecutor jobExecutor) {

        this.scheduledJob = scheduledJob;
        this.executionService = executionService;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public void run() {

        Long jobId = scheduledJob.getJobId();
        Long executionId = scheduledJob.getExecutionId();

        String workerName =
                Thread.currentThread().getName();

        System.out.println(
                "Worker " + workerName
                        + " started Job " + jobId
        );

        try {

            // QUEUED → RUNNING
            executionService.markRunning(
                    jobId,
                    executionId
            );

            // Execute actual job
            jobExecutor.execute(scheduledJob);

            // RUNNING → COMPLETED
            executionService.markCompleted(
                    jobId,
                    executionId
            );

            System.out.println(
                    "Worker " + workerName
                            + " completed Job " + jobId
            );

        } catch (Exception e) {

            System.err.println(
                    "Worker " + workerName
                            + " failed Job " + jobId
                            + ": " + e.getMessage()
            );

            try {

                // RUNNING → FAILED
                executionService.markFailed(
                        jobId,
                        executionId
                );

            } catch (Exception statusException) {

                System.err.println(
                        "Failed to mark Job "
                                + jobId
                                + " as FAILED: "
                                + statusException.getMessage()
                );
            }
        }
    }
}