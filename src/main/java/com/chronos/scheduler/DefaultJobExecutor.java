package com.chronos.scheduler;

import org.springframework.stereotype.Component;

@Component
public class DefaultJobExecutor implements JobExecutor {

    @Override
    public void execute(ScheduledJob job) throws Exception {

        System.out.println(
                "Executing Job " + job.getJobId()
                        + " | Execution " + job.getExecutionId()
        );

        // Temporary execution simulation.
        // Real job logic will be added later.
    }
}