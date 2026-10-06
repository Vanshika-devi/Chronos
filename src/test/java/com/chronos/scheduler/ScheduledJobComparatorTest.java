package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduledJobComparatorTest {

    private final ScheduledJobComparator comparator =
            new ScheduledJobComparator();

    @Test
    void higherPriorityShouldComeFirst() {

        ScheduledJob lowPriority =
                new ScheduledJob(
                        1L,
                        101L,
                        1,
                        1
                );

        ScheduledJob highPriority =
                new ScheduledJob(
                        2L,
                        102L,
                        5,
                        2
                );

        List<ScheduledJob> jobs =
                new ArrayList<>(
                        List.of(
                                lowPriority,
                                highPriority
                        )
                );

        jobs.sort(comparator);

        assertEquals(
                highPriority,
                jobs.get(0)
        );

        assertEquals(
                lowPriority,
                jobs.get(1)
        );
    }

    @Test
    void samePriorityShouldUseSequenceNumber() {

        ScheduledJob older =
                new ScheduledJob(
                        1L,
                        101L,
                        5,
                        1
                );

        ScheduledJob newer =
                new ScheduledJob(
                        2L,
                        102L,
                        5,
                        2
                );

        List<ScheduledJob> jobs =
                new ArrayList<>(
                        List.of(
                                newer,
                                older
                        )
                );

        jobs.sort(comparator);

        assertEquals(
                older,
                jobs.get(0)
        );

        assertEquals(
                newer,
                jobs.get(1)
        );
    }

    @Test
    void priorityShouldTakePrecedenceOverSequence() {

        ScheduledJob lowPriorityOlder =
                new ScheduledJob(
                        1L,
                        101L,
                        1,
                        1
                );

        ScheduledJob highPriorityNewer =
                new ScheduledJob(
                        2L,
                        102L,
                        5,
                        100
                );

        List<ScheduledJob> jobs =
                new ArrayList<>(
                        List.of(
                                lowPriorityOlder,
                                highPriorityNewer
                        )
                );

        jobs.sort(comparator);

        assertEquals(
                highPriorityNewer,
                jobs.get(0)
        );

        assertEquals(
                lowPriorityOlder,
                jobs.get(1)
        );
    }
}