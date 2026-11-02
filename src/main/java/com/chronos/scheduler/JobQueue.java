
package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
            Clock clock
    ) {

        this.priorityCalculator = priorityCalculator;
        this.fairnessPolicy = fairnessPolicy;
        this.clock = clock;
    }

    // Add a job that is immediately eligible.
    public void add(ScheduledJob scheduledJob) {

        enqueue(scheduledJob, Instant.now(clock));
    }

    // Add a job that becomes eligible after a delay.
    public void addDelayed(
            ScheduledJob scheduledJob,
            Duration delay
    ) {

        if (delay == null || delay.isNegative()) {
            throw new IllegalArgumentException(
                    "Delay cannot be null or negative."
            );
        }

        Instant availableAt =
                Instant.now(clock).plus(delay);

        enqueue(scheduledJob, availableAt);
    }

    private void enqueue(
            ScheduledJob scheduledJob,
            Instant availableAt
    ) {

        if (scheduledJob == null) {
            throw new IllegalArgumentException(
                    "Scheduled job cannot be null."
            );
        }

        Instant queuedAt = Instant.now(clock);

        scheduledJob.markQueued(queuedAt);
        scheduledJob.markAvailableAt(availableAt);

        lock.lock();

        try {

            jobs.add(scheduledJob);

            // Wake scheduler in case this job is
            // available earlier than its current wait.
            notEmpty.signalAll();

        } finally {
            lock.unlock();
        }
    }

    public ScheduledJob take() throws InterruptedException {

        lock.lockInterruptibly();

        try {

            while (true) {

                if (jobs.isEmpty()) {
                    notEmpty.await();
                    continue;
                }

                Instant now = Instant.now(clock);

                int bestIndex =
                        findBestJobIndex(false, now);

                // An eligible job was found.
                if (bestIndex >= 0) {

                    ScheduledJob selectedJob =
                            jobs.remove(bestIndex);

                    fairnessPolicy.recordSelection(
                            selectedJob.getSchedulingGroup()
                    );

                    return selectedJob;
                }

                // No job is currently eligible.
                // Wait until the earliest job becomes available.
                Instant nextAvailableAt =
                        findNextAvailableTime();

                long waitNanos;

                try {

                    waitNanos = Duration.between(
                            now,
                            nextAvailableAt
                    ).toNanos();

                } catch (ArithmeticException e) {

                    waitNanos = Long.MAX_VALUE;
                }

                if (waitNanos > 0) {
                    notEmpty.awaitNanos(waitNanos);
                }
            }

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

            return getBestAvailableJob();

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

    private ScheduledJob getBestAvailableJob() {

        int bestIndex = findBestJobIndex(
                false,
                Instant.now(clock)
        );

        return bestIndex == -1
                ? null
                : jobs.get(bestIndex);
    }

    private Instant findNextAvailableTime() {

        Instant earliest = null;

        for (ScheduledJob job : jobs) {

            Instant availableAt =
                    job.getAvailableAt();

            if (earliest == null
                    || availableAt.isBefore(earliest)) {

                earliest = availableAt;
            }
        }

        return earliest;
    }

    private int findBestJobIndex(
            boolean recordFairnessSelection,
            Instant now
    ) {

        List<ScheduledJob> eligibleJobs =
                new ArrayList<>();

        for (ScheduledJob job : jobs) {

            if (!job.getAvailableAt().isAfter(now)) {
                eligibleJobs.add(job);
            }
        }

        if (eligibleJobs.isEmpty()) {
            return -1;
        }

        int highestEffectivePriority =
                findHighestEffectivePriority(eligibleJobs);

        List<ScheduledJob> candidates =
                findCandidates(
                        eligibleJobs,
                        highestEffectivePriority
                );

        Set<String> eligibleGroupSet =
                new HashSet<>();

        for (ScheduledJob job : candidates) {

            eligibleGroupSet.add(
                    job.getSchedulingGroup()
            );
        }

        List<String> eligibleGroups =
                new ArrayList<>(eligibleGroupSet);

        String selectedGroup =
                recordFairnessSelection
                        ? fairnessPolicy.selectGroup(
                        new ArrayList<>(eligibleGroups)
                )
                        : fairnessPolicy.previewGroup(
                        new ArrayList<>(eligibleGroups)
                );

        return findBestJobInGroup(
                candidates,
                selectedGroup
        );
    }

    private int findHighestEffectivePriority(
            List<ScheduledJob> eligibleJobs
    ) {

        int highestPriority = Integer.MIN_VALUE;

        for (ScheduledJob job : eligibleJobs) {

            int effectivePriority =
                    priorityCalculator.calculateEffectivePriority(job);

            if (effectivePriority > highestPriority) {
                highestPriority = effectivePriority;
            }
        }

        return highestPriority;
    }

    private List<ScheduledJob> findCandidates(
            List<ScheduledJob> eligibleJobs,
            int highestEffectivePriority
    ) {

        List<ScheduledJob> candidates =
                new ArrayList<>();

        for (ScheduledJob job : eligibleJobs) {

            int effectivePriority =
                    priorityCalculator.calculateEffectivePriority(job);

            if (effectivePriority == highestEffectivePriority) {
                candidates.add(job);
            }
        }

        return candidates;
    }

    private int findBestJobInGroup(
            List<ScheduledJob> candidates,
            String selectedGroup
    ) {

        int bestIndex = -1;

        for (ScheduledJob job : candidates) {

            if (!job.getSchedulingGroup()
                    .equals(selectedGroup)) {
                continue;
            }

            int currentIndex = jobs.indexOf(job);

            if (bestIndex == -1) {
                bestIndex = currentIndex;
                continue;
            }

            ScheduledJob bestJob =
                    jobs.get(bestIndex);

            if (job.getSequenceNumber()
                    < bestJob.getSequenceNumber()) {

                bestIndex = currentIndex;
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

    public boolean remove(Long jobId, Long executionId) {

        if (jobId == null || executionId == null) {
            throw new IllegalArgumentException(
                    "Job ID and Execution ID cannot be null."
            );
        }

        lock.lock();

        try {
            boolean removed = jobs.removeIf(job ->
                    job.getJobId().equals(jobId)
                            && job.getExecutionId().equals(executionId)
            );

            if (removed) {
                notEmpty.signalAll();
            }

            return removed;

        } finally {
            lock.unlock();
        }
    }
}
