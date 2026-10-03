package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.util.concurrent.PriorityBlockingQueue;

@Component
public class JobQueue {

    private final PriorityBlockingQueue<ScheduledJob> queue;

    public JobQueue() {

        this.queue = new PriorityBlockingQueue<>(
                11,
                new ScheduledJobComparator()
        );
    }

    public void add(ScheduledJob scheduledJob) {

        queue.put(scheduledJob);
    }

    public ScheduledJob take()
            throws InterruptedException {

        return queue.take();
    }

    public ScheduledJob peek() {

        return queue.peek();
    }

    public boolean isEmpty() {

        return queue.isEmpty();
    }

    public int size() {

        return queue.size();
    }
}