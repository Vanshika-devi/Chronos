package com.chronos.scheduler;

import com.chronos.model.Job;

import java.util.Comparator;

public class JobPriorityComparator implements Comparator<Job> {

    @Override
    public int compare(Job first, Job second) {

        return Integer.compare(
                second.getPriority(),
                first.getPriority()
        );
    }
}