package com.chronos.worker;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.service.ExecutionService;

import java.time.LocalDateTime;

public class JobTask implements Runnable {

    private final Job job;
    private final Execution execution;
    private final ExecutionService executionService;

    public JobTask(
            Job job,
            Execution execution,
            ExecutionService executionService) {

        this.job = job;
        this.execution = execution;
        this.executionService = executionService;
    }

    @Override
    public void run() {

        String threadName =
                Thread.currentThread().getName();

        System.out.println(
                "Worker " + threadName
                        + " started Job "
                        + job.getId()
        );

        try {

            // Temporary simulation of job work
            Thread.sleep(2000);

            executionService.markCompleted(
                    execution,
                    job
            );

            System.out.println(
                    "Worker " + threadName
                            + " completed Job "
                            + job.getId()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            executionService.markFailed(
                    execution,
                    job
            );

            System.out.println(
                    "Worker " + threadName
                            + " interrupted Job "
                            + job.getId()
            );
        }
    }
}