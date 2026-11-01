
package com.chronos.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ExponentialBackoffPolicyTest {

    private ExponentialBackoffPolicy backoffPolicy;

    @BeforeEach
    void setUp() {
        backoffPolicy = new ExponentialBackoffPolicy();
    }

    @Test
    void firstRetryShouldWaitTwoSeconds() {
        assertEquals(
                Duration.ofSeconds(2),
                backoffPolicy.getDelayForAttempt(1)
        );
    }

    @Test
    void secondRetryShouldWaitFourSeconds() {
        assertEquals(
                Duration.ofSeconds(4),
                backoffPolicy.getDelayForAttempt(2)
        );
    }

    @Test
    void thirdRetryShouldWaitEightSeconds() {
        assertEquals(
                Duration.ofSeconds(8),
                backoffPolicy.getDelayForAttempt(3)
        );
    }

    @Test
    void delayShouldContinueDoubling() {
        assertEquals(
                Duration.ofSeconds(16),
                backoffPolicy.getDelayForAttempt(4)
        );

        assertEquals(
                Duration.ofSeconds(32),
                backoffPolicy.getDelayForAttempt(5)
        );
    }

    @Test
    void delayShouldNeverExceedMaximumOf300Seconds() {
        assertEquals(
                Duration.ofSeconds(300),
                backoffPolicy.getDelayForAttempt(20)
        );
    }

    @Test
    void veryLargeAttemptShouldStillRespectMaximumDelay() {
        assertEquals(
                Duration.ofSeconds(300),
                backoffPolicy.getDelayForAttempt(100)
        );
    }

    @Test
    void zeroAttemptShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> backoffPolicy.getDelayForAttempt(0)
        );
    }

    @Test
    void negativeAttemptShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> backoffPolicy.getDelayForAttempt(-1)
        );
    }
}
