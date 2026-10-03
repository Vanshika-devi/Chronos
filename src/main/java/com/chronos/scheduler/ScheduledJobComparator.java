package com.chronos.scheduler;

import java.util.Comparator;

public class ScheduledJobComparator
        implements Comparator<ScheduledJob> {

    @Override
    public int compare(
            ScheduledJob first,
            ScheduledJob second) {

        return Integer.compare(
                second.getPriority(),
                first.getPriority()
        );
    }
}