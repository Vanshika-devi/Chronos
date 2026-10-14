package com.chronos.scheduler;

import com.chronos.service.JobExecutionService;
import org.springframework.stereotype.Component;

@Component
public class Scheduler implements Runnable {

    private final JobQueue jobQueue;
    private final JobExecutionService jobExecutionService;

    private volatile boolean running = false;

    private Thread schedulerThread;

    public Scheduler(
            JobQueue jobQueue,
            JobExecutionService jobExecutionService) {

        this.jobQueue = jobQueue;
        this.jobExecutionService = jobExecutionService;
    }

    public synchronized void start() {

        if (running) {
            return;
        }

        running = true;

        schedulerThread =
                new Thread(
                        this,
                        "chronos-scheduler"
                );

        schedulerThread.start();
    }

    public synchronized void stop() {

        running = false;

        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {

        while (running) {

            try {

                ScheduledJob scheduledJob =
                        jobQueue.take();

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
            }
        }

        running = false;
    }
}