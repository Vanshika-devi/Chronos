package com.chronos.scheduler;

import com.chronos.service.ExecutionService;
import org.springframework.stereotype.Component;

@Component
public class DefaultJobExecutor implements JobExecutor {

    private final ExecutionService executionService;

    public DefaultJobExecutor(
            ExecutionService executionService
    ) {
        this.executionService = executionService;
    }

    @Override
    public void execute(ScheduledJob job) throws Exception {

        executionService.markRunning(
                job.getJobId(),
                job.getExecutionId()
        );

        try {

            // Actual job execution comes next.

            executionService.markCompleted(
                    job.getJobId(),
                    job.getExecutionId()
            );

        } catch (Exception e) {

            executionService.markFailed(
                    job.getJobId(),
                    job.getExecutionId()
            );

            throw e;
        }
    }
}