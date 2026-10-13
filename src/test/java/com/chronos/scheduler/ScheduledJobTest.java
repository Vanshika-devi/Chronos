
package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScheduledJobTest {

    @Test
    void shouldRejectNullSchedulingGroup() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ScheduledJob(
                        1L, 1L, 5, 1L, null
                )
        );
    }

    @Test
    void shouldRejectBlankSchedulingGroup() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ScheduledJob(
                        1L, 1L, 5, 1L, "   "
                )
        );
    }

    @Test
    void shouldPreserveValidSchedulingGroup() {

        ScheduledJob job = new ScheduledJob(
                1L, 1L, 5, 1L, "TEAM_A"
        );

        assertEquals(
                "TEAM_A",
                job.getSchedulingGroup()
        );
    }
}
