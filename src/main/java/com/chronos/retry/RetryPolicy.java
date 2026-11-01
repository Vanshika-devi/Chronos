
package com.chronos.retry;

public interface RetryPolicy {

    boolean shouldRetry(
            int currentAttempt,
            int maxAttempts
    );
}
