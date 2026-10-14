package com.chronos.service;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.scheduler.JobQueue;
import com.chronos.scheduler.ScheduledJob;
import com.chronos.scheduler.SequenceGenerator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobRecoveryService implements ApplicationRunner {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final JobQueue jobQueue;
    private final SequenceGenerator sequenceGenerator;

    public JobRecoveryService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            JobQueue jobQueue,
            SequenceGenerator sequenceGenerator) {

        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.jobQueue = jobQueue;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void run(ApplicationArguments args) {

        List<Job> queuedJobs =
                jobRepository.findByStatus(JobStatus.QUEUED);

        for (Job job : queuedJobs) {

            /*
             * Find the latest queued execution
             * belonging to this job.
             */
            List<Execution> executions =
                    executionRepository.findByJobId(job.getId());

            Execution queuedExecution = executions.stream()
                    .filter(execution ->
                            execution.getStatus()
                                    == ExecutionStatus.QUEUED)
                    .findFirst()
                    .orElse(null);

            /*
             * If there is no queued execution,
             * there is nothing valid to recover.
             */
            if (queuedExecution == null) {
                continue;
            }

            long sequenceNumber =
                    sequenceGenerator.next();

            ScheduledJob scheduledJob =
                    new ScheduledJob(
                            job.getId(),
                            queuedExecution.getId(),
                            job.getPriority(),
                            sequenceNumber,
                            "DEFAULT"
                    );

            jobQueue.add(scheduledJob);
        }
    }
}