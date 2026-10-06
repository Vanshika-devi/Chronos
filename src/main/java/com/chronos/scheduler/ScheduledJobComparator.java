package com.chronos.scheduler;

import java.util.Comparator;

public class ScheduledJobComparator
        implements Comparator<ScheduledJob> {

    @Override
    public int compare(
            ScheduledJob first,
            ScheduledJob second) {

        /*
         * Rule 1:
         * Higher priority comes first.
         *
         * Example:
         * priority 5 > priority 3
         */
        int priorityComparison =
                Integer.compare(
                        second.getPriority(),
                        first.getPriority()
                );

        /*
         * If priorities are different,
         * priority completely determines the order.
         */
        if (priorityComparison != 0) {
            return priorityComparison;
        }

        /*
         * Rule 2:
         * Same priority -> FIFO.
         *
         * Smaller sequence number means
         * the job entered the queue earlier.
         */
        return Long.compare(
                first.getSequenceNumber(),
                second.getSequenceNumber()
        );
    }
}