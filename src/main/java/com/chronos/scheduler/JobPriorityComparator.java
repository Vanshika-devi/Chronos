package com.chronos.scheduler;

import com.chronos.model.Job;

import java.util.Comparator;

public class JobPriorityComparator implements Comparator<ScheduledJob> {

    @Override
    public int compare(ScheduledJob first, ScheduledJob second) {

        return Integer.compare(
                second.getPriority(),
                first.getPriority()
        );
    }
}