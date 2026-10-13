
package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FairnessPolicyTest {

    @Test
    void shouldAllowTwoConsecutiveSelections() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.selectGroup(List.of("A", "B")));
        assertEquals("A", policy.selectGroup(List.of("A", "B")));
    }

    @Test
    void shouldSwitchGroupAfterConsecutiveLimit() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.selectGroup(List.of("A", "B")));
        assertEquals("A", policy.selectGroup(List.of("A", "B")));
        assertEquals("B", policy.selectGroup(List.of("A", "B")));
        assertEquals("B", policy.selectGroup(List.of("A", "B")));
        assertEquals("A", policy.selectGroup(List.of("A", "B")));
    }

    @Test
    void shouldContinueWhenOnlyOneGroupIsAvailable() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.selectGroup(List.of("A")));
        assertEquals("A", policy.selectGroup(List.of("A")));
        assertEquals("A", policy.selectGroup(List.of("A")));
    }

    @Test
    void shouldHandlePreviousGroupBecomingUnavailable() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.selectGroup(List.of("A", "B")));
        assertEquals("B", policy.selectGroup(List.of("B")));
    }

    @Test
    void shouldNotDependOnInputGroupOrder() {
        FairnessPolicy first = new FairnessPolicy(2);
        FairnessPolicy second = new FairnessPolicy(2);

        assertEquals(
                first.selectGroup(List.of("B", "A")),
                second.selectGroup(List.of("A", "B"))
        );
    }

    @Test
    void shouldHandleEmptyEligibleGroups() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertNull(policy.selectGroup(List.of()));
    }

    @Test
    void shouldHandleNullEligibleGroups() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertNull(policy.selectGroup(null));
    }

    @Test
    void shouldRejectZeroConsecutiveLimit() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new FairnessPolicy(0)
        );
    }

    @Test
    void shouldRejectNegativeConsecutiveLimit() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new FairnessPolicy(-1)
        );
    }

    @Test
    void shouldSwitchWhenPreviousGroupDisappearsAndReturns() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.selectGroup(List.of("A", "B")));
        assertEquals("A", policy.selectGroup(List.of("A", "B")));

        // Only B is eligible, so the policy must select B.
        assertEquals("B", policy.selectGroup(List.of("B")));

        // A returns. The previous consecutive count for A
        // must not cause A to bypass the newly selected B.
        assertEquals("B", policy.selectGroup(List.of("A", "B")));
    }

    @Test
    void previewShouldNotAdvanceFairnessState() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.selectGroup(List.of("A", "B")));
        assertEquals("A", policy.selectGroup(List.of("A", "B")));

        // Preview must not consume another selection.
        assertEquals("B", policy.previewGroup(List.of("A", "B")));

        // The next actual selection must still be B.
        assertEquals("B", policy.selectGroup(List.of("A", "B")));
    }

    @Test
    void shouldRejectNullRecordedGroup() {
        FairnessPolicy policy = new FairnessPolicy(2);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> policy.recordSelection(null)
        );
    }

    @Test
    void shouldApplyConsecutiveLimitAfterRecordingSelection() {
        FairnessPolicy policy = new FairnessPolicy(2);

        assertEquals("A", policy.previewGroup(List.of("A", "B")));

        // Previewing doesn't count.
        assertEquals("A", policy.previewGroup(List.of("A", "B")));

        policy.recordSelection("A");
        policy.recordSelection("A");

        assertEquals("B", policy.previewGroup(List.of("A", "B")));
    }


}
