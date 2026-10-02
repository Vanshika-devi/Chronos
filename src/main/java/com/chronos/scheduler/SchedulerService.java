package com.chronos.scheduler;

import com.chronos.model.Job;
import com.chronos.worker.WorkerPool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SchedulerService {

    private final JobQueue jobQueue;
    private final WorkerPool workerPool;

    private final ExecutorService schedulerExecutor;

    public SchedulerService(
            JobQueue jobQueue,
            WorkerPool workerPool) {

        this.jobQueue = jobQueue;
        this.workerPool = workerPool;

        this.schedulerExecutor =
                Executors.newSingleThreadExecutor();
    }

    @PostConstruct
    public void start() {

        schedulerExecutor.submit(
                this::schedulerLoop
        );
    }

    private void schedulerLoop() {

        while (!Thread.currentThread().isInterrupted()) {

            Job job = jobQueue.poll();

            if (job == null) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }

                continue;
            }

            workerPool.submit(
                    job,
                    null
            );
        }
    }

    public void schedule(Job job) {

        jobQueue.add(job);

        System.out.println(
                "Job " + job.getId()
                        + " added to scheduler queue"
        );
    }

    @PreDestroy
    public void shutdown() {

        schedulerExecutor.shutdownNow();
    }
}