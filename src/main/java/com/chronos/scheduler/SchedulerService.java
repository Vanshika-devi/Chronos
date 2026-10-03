package com.chronos.scheduler;

import com.chronos.service.JobExecutionService;
import com.chronos.worker.WorkerPool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class SchedulerService {

    private final JobQueue jobQueue;
    private final JobExecutionService jobExecutionService;
    private final WorkerPool workerPool;

    private Thread schedulerThread;

    private volatile boolean running = true;

    public SchedulerService(
            JobQueue jobQueue,
            JobExecutionService jobExecutionService,
            WorkerPool workerPool) {

        this.jobQueue = jobQueue;
        this.jobExecutionService = jobExecutionService;
        this.workerPool = workerPool;
    }

    @PostConstruct
    public void start() {

        schedulerThread = new Thread(
                this::schedulerLoop,
                "chronos-scheduler"
        );

        schedulerThread.start();

        System.out.println(
                "Chronos Scheduler started."
        );
    }

    private void schedulerLoop() {

        while (running) {

            try {

                ScheduledJob scheduledJob =
                        jobQueue.take();

                System.out.println(
                        "Scheduler picked Job "
                                + scheduledJob.getJobId()
                                + " with priority "
                                + scheduledJob.getPriority()
                );

                jobExecutionService.dispatchJob(
                        scheduledJob.getJobId(),
                        scheduledJob.getExecutionId()
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                break;

            } catch (Exception e) {

                System.err.println(
                        "Scheduler failed to dispatch job: "
                                + e.getMessage()
                );

                e.printStackTrace();
            }
        }

        System.out.println(
                "Chronos Scheduler stopped."
        );
    }

    @PreDestroy
    public void stop() {

        System.out.println(
                "Chronos Scheduler shutdown initiated."
        );

        running = false;

        if (schedulerThread != null) {

            schedulerThread.interrupt();

            try {

                schedulerThread.join(5000);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }
        }

        workerPool.shutdown();

        System.out.println(
                "Chronos Scheduler shutdown complete."
        );
    }
}