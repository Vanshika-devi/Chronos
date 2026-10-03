package com.chronos.worker;

import com.chronos.service.ExecutionService;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

@Component
public class WorkerPool {

    private final ExecutorService executorService;
    private final ExecutionService executionService;

    public WorkerPool(
            ExecutionService executionService) {

        this.executionService = executionService;

        this.executorService =
                Executors.newFixedThreadPool(3);
    }

    public void submit(
            Long jobId,
            Long executionId) {

        JobTask task =
                new JobTask(
                        jobId,
                        executionId,
                        executionService
                );

        try {

            executorService.submit(task);

        } catch (RejectedExecutionException e) {

            System.err.println(
                    "Job " + jobId
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
                    java.util.concurrent.TimeUnit.SECONDS)) {

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