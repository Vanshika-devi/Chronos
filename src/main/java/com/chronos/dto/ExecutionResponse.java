
package com.chronos.dto;

import java.time.LocalDateTime;

public record ExecutionResponse(
        Long id,
        Long jobId,
        String status,
        int attemptNumber,
        String failureReason,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
