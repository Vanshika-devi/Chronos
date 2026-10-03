package com.chronos.worker;

import com.chronos.service.ExecutionService;

public class JobTask implements Runnable {

    private final Long jobId;
    private final Long executionId;
    private final ExecutionService executionService;

    public JobTask(
            Long jobId,
            Long executionId,
            ExecutionService executionService) {

        this.jobId = jobId;
        this.executionId = executionId;
        this.executionService = executionService;
    }

    @Override
    public void run() {

        String threadName =
                Thread.currentThread().getName();

        System.out.println(
                "Worker " + threadName
                        + " started Job "
                        + jobId
        );

        try {

            executionService.markRunning(
                    jobId,
                    executionId
            );

            // Temporary simulation
            Thread.sleep(2000);

            executionService.markCompleted(
                    jobId,
                    executionId
            );

            System.out.println(
                    "Worker " + threadName
                            + " completed Job "
                            + jobId
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            executionService.markFailed(
                    jobId,
                    executionId
            );

            System.out.println(
                    "Worker " + threadName
                            + " interrupted Job "
                            + jobId
            );
        }
    }
}