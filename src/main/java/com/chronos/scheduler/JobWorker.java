package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class JobWorker {

    private final JobQueue jobQueue;
    private final JobExecutor jobExecutor;
    private final ExecutorService executorService;

    public JobWorker(
            JobQueue jobQueue,
            JobExecutor jobExecutor
    ) {
        this.jobQueue = jobQueue;
        this.jobExecutor = jobExecutor;
        this.executorService =
                Executors.newFixedThreadPool(4);
    }

    public void start() {

        for (int i = 0; i < 4; i++) {
            executorService.submit(this::workerLoop);
        }
    }

    private void workerLoop() {

        while (!Thread.currentThread().isInterrupted()) {

            try {

                ScheduledJob job = jobQueue.take();

                jobExecutor.execute(job);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            } catch (Exception e) {

                // Temporary handling.
                // Retry/failure policy comes next.
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}