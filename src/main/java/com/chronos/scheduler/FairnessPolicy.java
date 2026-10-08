package com.chronos.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class FairnessPolicy {

    private final int maxConsecutive;

    private String lastSelectedGroup;
    private int consecutiveCount;

    public FairnessPolicy(
            @Value("${chronos.scheduler.fairness.max-consecutive:2}")
            int maxConsecutive) {

        if (maxConsecutive <= 0) {
            throw new IllegalArgumentException(
                    "Maximum consecutive jobs must be greater than zero."
            );
        }

        this.maxConsecutive = maxConsecutive;
    }

    public synchronized String selectGroup(
            List<String> eligibleGroups) {

        if (eligibleGroups == null
                || eligibleGroups.isEmpty()) {

            return null;
        }

        List<String> groups =
                new ArrayList<>(eligibleGroups);

        Collections.sort(groups);

        /*
         * First selection.
         */
        if (lastSelectedGroup == null) {

            return selectAndRecord(
                    groups.get(0)
            );
        }

        /*
         * If we have already selected the same
         * group too many times consecutively,
         * switch to another available group.
         */
        if (consecutiveCount >= maxConsecutive
                && groups.size() > 1) {

            for (String group : groups) {

                if (!group.equals(lastSelectedGroup)) {

                    return selectAndRecord(group);
                }
            }
        }

        /*
         * Continue with the previous group if
         * it is still eligible.
         */
        if (groups.contains(lastSelectedGroup)) {

            return selectAndRecord(
                    lastSelectedGroup
            );
        }

        /*
         * Previous group disappeared.
         * Select the first deterministic group.
         */
        return selectAndRecord(
                groups.get(0)
        );
    }

    private String selectAndRecord(
            String group) {

        if (group.equals(lastSelectedGroup)) {

            consecutiveCount++;

        } else {

            lastSelectedGroup = group;
            consecutiveCount = 1;
        }

        return group;
    }
}