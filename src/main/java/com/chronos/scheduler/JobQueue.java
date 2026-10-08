package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final FairnessPolicy fairnessPolicy;
    private final Clock clock;

    public JobQueue(
            SchedulerPriorityCalculator priorityCalculator,
            FairnessPolicy fairnessPolicy,
            Clock clock) {

        this.priorityCalculator =
                priorityCalculator;

        this.fairnessPolicy =
                fairnessPolicy;

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

        int highestEffectivePriority =
                findHighestEffectivePriority();

        List<ScheduledJob> candidates =
                findCandidates(
                        highestEffectivePriority
                );

        Set<String> eligibleGroupSet =
                new HashSet<>();

        for (ScheduledJob job : candidates) {

            eligibleGroupSet.add(
                    job.getSchedulingGroup()
            );
        }

        String selectedGroup =
                fairnessPolicy.selectGroup(
                        new ArrayList<>(
                                eligibleGroupSet
                        )
                );

        return findBestJobInGroup(
                candidates,
                selectedGroup
        );
    }

    private int findHighestEffectivePriority() {

        int highestPriority =
                Integer.MIN_VALUE;

        for (ScheduledJob job : jobs) {

            int effectivePriority =
                    priorityCalculator
                            .calculateEffectivePriority(job);

            if (effectivePriority
                    > highestPriority) {

                highestPriority =
                        effectivePriority;
            }
        }

        return highestPriority;
    }

    private List<ScheduledJob> findCandidates(
            int highestEffectivePriority) {

        List<ScheduledJob> candidates =
                new ArrayList<>();

        for (ScheduledJob job : jobs) {

            int effectivePriority =
                    priorityCalculator
                            .calculateEffectivePriority(job);

            if (effectivePriority
                    == highestEffectivePriority) {

                candidates.add(job);
            }
        }

        return candidates;
    }

    private int findBestJobInGroup(
            List<ScheduledJob> candidates,
            String selectedGroup) {

        int bestIndex = -1;

        for (ScheduledJob job : candidates) {

            if (!job.getSchedulingGroup()
                    .equals(selectedGroup)) {
                continue;
            }

            int currentIndex =
                    jobs.indexOf(job);

            if (bestIndex == -1) {

                bestIndex =
                        currentIndex;

                continue;
            }

            ScheduledJob bestJob =
                    jobs.get(bestIndex);

            if (job.getSequenceNumber()
                    < bestJob.getSequenceNumber()) {

                bestIndex =
                        currentIndex;
            }
        }

        if (bestIndex == -1) {

            throw new IllegalStateException(
                    "Fairness policy selected a group "
                            + "that has no candidate job."
            );
        }

        return bestIndex;
    }
}