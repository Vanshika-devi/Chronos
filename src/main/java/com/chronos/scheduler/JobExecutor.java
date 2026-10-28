package com.chronos.scheduler;

public interface JobExecutor {

    void execute(ScheduledJob job) throws Exception;
}