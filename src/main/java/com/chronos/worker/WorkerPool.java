package com.chronos.worker;

import com.chronos.scheduler.JobExecutor;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.service.ExecutionService;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class WorkerPool {

    private final ExecutorService executorService;
    private final ExecutionService executionService;
    private final JobExecutor jobExecutor;

    public WorkerPool(
            ExecutionService executionService,
            JobExecutor jobExecutor) {

        this.executionService = executionService;
        this.jobExecutor = jobExecutor;

        this.executorService =
                Executors.newFixedThreadPool(3);
    }

    public void submit(ScheduledJob scheduledJob) {

        JobTask task =
                new JobTask(
                        scheduledJob,
                        executionService,
                        jobExecutor
                );

        try {

            executorService.submit(task);

        } catch (RejectedExecutionException e) {

            System.err.println(
                    "Job " + scheduledJob.getJobId()
                            + " was rejected because "
                            + "the worker pool is shutting down."
            );

            throw e;
        }
    }

    public void shutdown() {

        System.out.println(
                "WorkerPool shutdown initiated."
        );

        executorService.shutdown();

        try {

            if (!executorService.awaitTermination(
                    10,
                    TimeUnit.SECONDS)) {

                System.out.println(
                        "Workers did not finish within "
                                + "10 seconds."
                );

                executorService.shutdownNow();
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            executorService.shutdownNow();
        }

        System.out.println(
                "WorkerPool shutdown complete."
        );
    }
}