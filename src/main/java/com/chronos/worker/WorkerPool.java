package com.chronos.worker;

import com.chronos.service.ExecutionService;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WorkerPool {

    private final ExecutorService executorService;
    private final ExecutionService executionService;

    public WorkerPool(ExecutionService executionService) {

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

        executorService.submit(task);
    }

    public void shutdown() {

        executorService.shutdown();
    }
}