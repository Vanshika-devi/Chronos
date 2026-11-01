
package com.chronos.retry;

import org.springframework.stereotype.Component;

@Component
public class DefaultRetryPolicy implements RetryPolicy {

    @Override
    public boolean shouldRetry(
            int currentAttempt,
            int maxAttempts
    ) {

        if (currentAttempt < 1) {
            throw new IllegalArgumentException(
                    "Current attempt must be at least 1"
            );
        }

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "Maximum attempts must be at least 1"
            );
        }

        return currentAttempt < maxAttempts;
    }
}
