
package com.chronos.retry;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ExponentialBackoffPolicy {

    private static final long INITIAL_DELAY_SECONDS = 2;

    private static final long MAX_DELAY_SECONDS = 300;

    public Duration getDelayForAttempt(int failedAttempt) {

        if (failedAttempt < 1) {
            throw new IllegalArgumentException(
                    "Failed attempt must be at least 1."
            );
        }

        long delaySeconds = INITIAL_DELAY_SECONDS;

        for (int i = 1;
             i < failedAttempt
                     && delaySeconds < MAX_DELAY_SECONDS;
             i++) {

            delaySeconds = Math.min(
                    delaySeconds * 2,
                    MAX_DELAY_SECONDS
            );
        }

        return Duration.ofSeconds(delaySeconds);
    }
}
