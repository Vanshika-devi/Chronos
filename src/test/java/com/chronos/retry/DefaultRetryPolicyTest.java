package com.chronos.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRetryPolicyTest {

    private final RetryPolicy retryPolicy =
            new DefaultRetryPolicy();

    @Test
    void shouldRetryWhenAttemptsRemain() {

        assertTrue(
                retryPolicy.shouldRetry(1, 3)
        );
    }

    @Test
    void shouldNotRetryWhenMaximumAttemptsReached() {

        assertFalse(
                retryPolicy.shouldRetry(3, 3)
        );
    }

    @Test
    void shouldNotRetryWhenOnlyOneAttemptAllowed() {

        assertFalse(
                retryPolicy.shouldRetry(1, 1)
        );
    }

    @Test
    void shouldThrowWhenCurrentAttemptIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> retryPolicy.shouldRetry(0, 3)
        );
    }

    @Test
    void shouldThrowWhenMaximumAttemptsIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> retryPolicy.shouldRetry(1, 0)
        );
    }
}
