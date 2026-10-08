package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FairnessPolicyTest {

    @Test
    void shouldAllowTwoConsecutiveSelections() {

        FairnessPolicy policy =
                new FairnessPolicy(2);

        assertEquals(
                "A",
                policy.selectGroup(List.of("A", "B"))
        );

        assertEquals(
                "A",
                policy.selectGroup(List.of("A", "B"))
        );
    }

    @Test
    void shouldSwitchGroupAfterConsecutiveLimit() {

        FairnessPolicy policy =
                new FairnessPolicy(2);

        assertEquals(
                "A",
                policy.selectGroup(List.of("A", "B"))
        );

        assertEquals(
                "A",
                policy.selectGroup(List.of("A", "B"))
        );

        assertEquals(
                "B",
                policy.selectGroup(List.of("A", "B"))
        );

        assertEquals(
                "B",
                policy.selectGroup(List.of("A", "B"))
        );

        assertEquals(
                "A",
                policy.selectGroup(List.of("A", "B"))
        );
    }

    @Test
    void shouldContinueWhenOnlyOneGroupIsAvailable() {

        FairnessPolicy policy =
                new FairnessPolicy(2);

        assertEquals(
                "A",
                policy.selectGroup(List.of("A"))
        );

        assertEquals(
                "A",
                policy.selectGroup(List.of("A"))
        );

        assertEquals(
                "A",
                policy.selectGroup(List.of("A"))
        );
    }

    @Test
    void shouldHandlePreviousGroupBecomingUnavailable() {

        FairnessPolicy policy =
                new FairnessPolicy(2);

        assertEquals(
                "A",
                policy.selectGroup(List.of("A", "B"))
        );

        assertEquals(
                "B",
                policy.selectGroup(List.of("B"))
        );
    }

    @Test
    void shouldNotDependOnInputGroupOrder() {

        FairnessPolicy firstPolicy =
                new FairnessPolicy(2);

        FairnessPolicy secondPolicy =
                new FairnessPolicy(2);

        String firstSelection =
                firstPolicy.selectGroup(
                        List.of("B", "A")
                );

        String secondSelection =
                secondPolicy.selectGroup(
                        List.of("A", "B")
                );

        assertEquals(
                secondSelection,
                firstSelection
        );
    }
}