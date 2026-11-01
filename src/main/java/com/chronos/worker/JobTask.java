
package com.chronos.worker;

import com.chronos.retry.RetryService;
import com.chronos.scheduler.JobExecutor;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.service.ExecutionService;

public class JobTask implements Runnable {

    private final ScheduledJob scheduledJob;
    private final ExecutionService executionService;
    private final JobExecutor jobExecutor;
    private final RetryService retryService;

    public JobTask(
            ScheduledJob scheduledJob,
            ExecutionService executionService,
            JobExecutor jobExecutor,
            RetryService retryService
    ) {
        this.scheduledJob = scheduledJob;
        this.executionService = executionService;
        this.jobExecutor = jobExecutor;
        this.retryService = retryService;
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
                        + " | Attempt "
                        + scheduledJob.getSequenceNumber()
        );

        // Step 1: QUEUED -> RUNNING
        try {

            executionService.markRunning(
                    jobId,
                    executionId
            );

        } catch (Exception e) {

            System.err.println(
                    "Could not mark Job " + jobId
                            + " as RUNNING: " + e.getMessage()
            );

            return;
        }

        // Step 2: Execute actual job
        try {

            jobExecutor.execute(scheduledJob);

        } catch (Exception executionException) {

            System.err.println(
                    "Job " + jobId
                            + " failed: "
                            + executionException.getMessage()
            );

            handleFailure(executionException);

            return;
        }

        // Step 3: RUNNING -> COMPLETED
        try {

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
                    "Job " + jobId
                            + " executed, but completion "
                            + "could not be persisted: "
                            + e.getMessage()
            );
        }
    }

    private void handleFailure(Exception exception) {

        Long jobId = scheduledJob.getJobId();
        Long executionId = scheduledJob.getExecutionId();

        try {

            // Record failure first
            executionService.markFailed(
                    jobId,
                    executionId,
                    exception.getMessage()
            );

            // Ask retry service to schedule another attempt
            boolean retryScheduled =
                    retryService.scheduleRetry(scheduledJob);

            if (retryScheduled) {

                System.out.println(
                        "Retry scheduled for Job " + jobId
                );

            } else {

                System.out.println(
                        "Job " + jobId
                                + " exhausted all retry attempts."
                );
            }

        } catch (Exception retryException) {

            System.err.println(
                    "Failure handling failed for Job "
                            + jobId
                            + ": "
                            + retryException.getMessage()
            );
        }
    }
}
