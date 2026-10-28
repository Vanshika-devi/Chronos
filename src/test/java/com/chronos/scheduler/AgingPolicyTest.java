package com.chronos.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgingPolicyTest {

    private final AgingPolicy agingPolicy =
            new AgingPolicy(true, 100, 5);

    @Test
    void noWaitingTimeShouldGiveZeroBonus() {

        assertEquals(
                0,
                agingPolicy.calculateBonus(0)
        );
    }

    @Test
    void waitingTimeShouldIncreaseBonus() {

        assertEquals(
                1,
                agingPolicy.calculateBonus(100)
        );

        assertEquals(
                3,
                agingPolicy.calculateBonus(350)
        );
    }

    @Test
    void agingBonusShouldBeCapped() {

        assertEquals(
                5,
                agingPolicy.calculateBonus(1000)
        );

        assertEquals(
                5,
                agingPolicy.calculateBonus(10_000)
        );
    }

    @Test
    void disabledAgingShouldGiveZeroBonus() {

        AgingPolicy disabledPolicy =
                new AgingPolicy(false, 100, 5);

        assertEquals(
                0,
                disabledPolicy.calculateBonus(10_000)
        );
    }
    @Test
    void waitingTimeBelowThresholdShouldGiveZeroBonus() {
        assertEquals(
                0,
                agingPolicy.calculateBonus(99)
        );
    }

    @Test
    void waitingTimeAtThresholdShouldGiveOneBonus() {
        assertEquals(
                1,
                agingPolicy.calculateBonus(100)
        );
    }

    @Test
    void waitingTimeAtMultipleThresholdsShouldCalculateCorrectBonus() {
        assertEquals(
                2,
                agingPolicy.calculateBonus(200)
        );

        assertEquals(
                4,
                agingPolicy.calculateBonus(450)
        );
    }

    @Test
    void negativeWaitingTimeShouldNotGiveNegativeBonus() {
        assertEquals(
                0,
                agingPolicy.calculateBonus(-100)
        );
    }
    @Test
    void waitingTimeExactlyAtThresholdShouldGiveOneBonus() {
        AgingPolicy policy = new AgingPolicy(true, 100, 5);

        assertEquals(1, policy.calculateBonus(100));
    }

}