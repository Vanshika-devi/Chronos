package com.chronos.retry;

public interface RetryPolicy {

    boolean shouldRetry(
            int attemptNumber,
            int maxRetries
    );
}