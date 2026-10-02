package com.chronos.scheduler;

import com.chronos.model.Job;
import org.springframework.stereotype.Component;

import java.util.PriorityQueue;

@Component
public class JobQueue {

    private final PriorityQueue<Job> queue;

    public JobQueue() {

        this.queue = new PriorityQueue<>(
                new JobPriorityComparator()
        );
    }

    public void add(Job job) {
        queue.offer(job);
    }

    public Job poll() {
        return queue.poll();
    }

    public Job peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}