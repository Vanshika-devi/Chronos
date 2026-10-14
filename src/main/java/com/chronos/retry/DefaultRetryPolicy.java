package com.chronos.retry;

import org.springframework.stereotype.Component;

@Component
public class DefaultRetryPolicy implements RetryPolicy {

    @Override
    public boolean shouldRetry(
            int attemptNumber,
            int maxRetries) {

        return attemptNumber <= maxRetries;
    }
}