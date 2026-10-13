
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

        String selected = previewGroup(eligibleGroups);

        if (selected == null) {
            return null;
        }

        recordSelection(selected);

        return selected;
    }


    public synchronized String previewGroup(
            List<String> eligibleGroups) {

        if (eligibleGroups == null || eligibleGroups.isEmpty()) {
            return null;
        }

        List<String> groups = new ArrayList<>(eligibleGroups);
        Collections.sort(groups);

        if (lastSelectedGroup == null) {
            return groups.get(0);
        }

        if (consecutiveCount >= maxConsecutive
                && groups.size() > 1) {

            for (String group : groups) {
                if (!group.equals(lastSelectedGroup)) {
                    return group;
                }
            }
        }

        if (groups.contains(lastSelectedGroup)) {
            return lastSelectedGroup;
        }

        return groups.get(0);
    }

    public synchronized void recordSelection(String group) {

        if (group == null) {
            throw new IllegalArgumentException(
                    "Selected group cannot be null."
            );
        }

        if (group.equals(lastSelectedGroup)) {
            consecutiveCount++;
        } else {
            lastSelectedGroup = group;
            consecutiveCount = 1;
        }
    }
}
