package com.chronos.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRetryPolicyTest {

    private final RetryPolicy retryPolicy =
            new DefaultRetryPolicy();

    @Test
    void shouldRetryWhenAttemptsRemain() {

        assertTrue(
                retryPolicy.shouldRetry(1, 2)
        );

        assertTrue(
                retryPolicy.shouldRetry(2, 2)
        );
    }

    @Test
    void shouldNotRetryWhenRetriesAreExhausted() {

        assertFalse(
                retryPolicy.shouldRetry(3, 2)
        );
    }

    @Test
    void shouldNotRetryWhenMaxRetriesIsZero() {

        assertFalse(
                retryPolicy.shouldRetry(1, 0)
        );
    }

    @Test
    void shouldRetryFirstAttemptWhenRetriesAreAvailable() {

        assertTrue(
                retryPolicy.shouldRetry(1, 1)
        );
    }
}