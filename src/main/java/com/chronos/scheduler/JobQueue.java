package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class JobQueue {

    private final List<ScheduledJob> jobs =
            new ArrayList<>();

    private final ReentrantLock lock =
            new ReentrantLock();

    private final Condition notEmpty =
            lock.newCondition();

    private final SchedulerPriorityCalculator priorityCalculator;
    private final Clock clock;

    public JobQueue(
            SchedulerPriorityCalculator priorityCalculator,
            Clock clock) {

        this.priorityCalculator =
                priorityCalculator;

        this.clock = clock;
    }

    public void add(ScheduledJob scheduledJob) {

        Instant queuedAt =
                Instant.now(clock);

        scheduledJob.markQueued(queuedAt);

        lock.lock();

        try {

            jobs.add(scheduledJob);

            notEmpty.signal();

        } finally {

            lock.unlock();
        }
    }

    public ScheduledJob take()
            throws InterruptedException {

        lock.lockInterruptibly();

        try {

            while (jobs.isEmpty()) {

                notEmpty.await();
            }

            int bestIndex =
                    findBestJobIndex();

            return jobs.remove(bestIndex);

        } finally {

            lock.unlock();
        }
    }

    public ScheduledJob peek() {

        lock.lock();

        try {

            if (jobs.isEmpty()) {
                return null;
            }

            int bestIndex =
                    findBestJobIndex();

            return jobs.get(bestIndex);

        } finally {

            lock.unlock();
        }
    }

    public boolean isEmpty() {

        lock.lock();

        try {
            return jobs.isEmpty();

        } finally {
            lock.unlock();
        }
    }

    public int size() {

        lock.lock();

        try {
            return jobs.size();

        } finally {
            lock.unlock();
        }
    }

    private int findBestJobIndex() {

        int bestIndex = 0;

        for (int i = 1; i < jobs.size(); i++) {

            ScheduledJob current =
                    jobs.get(i);

            ScheduledJob best =
                    jobs.get(bestIndex);

            if (comesBefore(current, best)) {
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private boolean comesBefore(
            ScheduledJob first,
            ScheduledJob second) {

        int firstEffectivePriority =
                priorityCalculator
                        .calculateEffectivePriority(first);

        int secondEffectivePriority =
                priorityCalculator
                        .calculateEffectivePriority(second);

        /*
         * Higher effective priority wins.
         */
        if (firstEffectivePriority
                != secondEffectivePriority) {

            return firstEffectivePriority
                    > secondEffectivePriority;
        }

        /*
         * Same effective priority:
         * older sequence number wins.
         */
        return first.getSequenceNumber()
                < second.getSequenceNumber();
    }
}