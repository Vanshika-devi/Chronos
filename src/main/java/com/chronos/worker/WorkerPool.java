package com.chronos.worker;

import com.chronos.model.Execution;
import com.chronos.model.Job;
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
            Job job,
            Execution execution) {

        JobTask task =
                new JobTask(
                        job,
                        execution,
                        executionService
                );

        executorService.submit(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}