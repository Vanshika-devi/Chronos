package com.chronos.model;

public enum JobStatus {

    CREATED,
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED;

    /**
     * Checks whether moving from the current status
     * to the next status is allowed.
     */
    public boolean canTransitionTo(JobStatus next) {

        if (next == null) {
            return false;
        }

        return switch (this) {

            case CREATED ->
                    next == QUEUED;

            case QUEUED ->
                    next == RUNNING ||
                            next == CANCELLED;

            case RUNNING ->
                    next == COMPLETED ||
                            next == FAILED;

            case FAILED ->
                    next == QUEUED;

            case COMPLETED, CANCELLED ->
                    false;
        };
    }
}