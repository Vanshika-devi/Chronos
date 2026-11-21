
package com.chronos.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobStatusTest {

    @Test
    void shouldAllowCreatedToQueued() {
        assertTrue(
                JobStatus.CREATED.canTransitionTo(JobStatus.QUEUED)
        );
    }

    @Test
    void shouldAllowQueuedToRunning() {
        assertTrue(
                JobStatus.QUEUED.canTransitionTo(JobStatus.RUNNING)
        );
    }

    @Test
    void shouldAllowQueuedToCancelled() {
        assertTrue(
                JobStatus.QUEUED.canTransitionTo(JobStatus.CANCELLED)
        );
    }

    @Test
    void shouldAllowRunningToCompleted() {
        assertTrue(
                JobStatus.RUNNING.canTransitionTo(JobStatus.COMPLETED)
        );
    }

    @Test
    void shouldAllowRunningToFailed() {
        assertTrue(
                JobStatus.RUNNING.canTransitionTo(JobStatus.FAILED)
        );
    }

    @Test
    void shouldAllowFailedToQueuedForRetry() {
        assertTrue(
                JobStatus.FAILED.canTransitionTo(JobStatus.QUEUED)
        );
    }

    @Test
    void shouldRejectCompletedToRunning() {
        assertFalse(
                JobStatus.COMPLETED.canTransitionTo(JobStatus.RUNNING)
        );
    }

    @Test
    void shouldRejectCancelledToQueued() {
        assertFalse(
                JobStatus.CANCELLED.canTransitionTo(JobStatus.QUEUED)
        );
    }

    @Test
    void shouldRejectCreatedToRunning() {
        assertFalse(
                JobStatus.CREATED.canTransitionTo(JobStatus.RUNNING)
        );
    }

    @Test
    void shouldRejectNullStatus() {
        assertFalse(
                JobStatus.CREATED.canTransitionTo(null)
        );
    }
}
