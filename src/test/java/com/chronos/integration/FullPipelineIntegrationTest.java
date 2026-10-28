package com.chronos.integration;

import com.chronos.model.Execution;
import com.chronos.model.ExecutionStatus;
import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.repository.ExecutionRepository;
import com.chronos.repository.JobRepository;
import com.chronos.service.JobExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FullPipelineIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Test
    void completeJobExecutionPipeline() throws Exception {

        System.out.println();
        System.out.println("========================================");
        System.out.println("   CHRONOS FULL PIPELINE TEST");
        System.out.println("========================================");

        // ------------------------------------------------
        // 1. CREATE JOB
        // ------------------------------------------------

        Job job = new Job(
                null,
                "pipeline-test-job",
                5,
                JobStatus.CREATED
        );

        job = jobRepository.save(job);

        System.out.println();
        System.out.println("[1] JOB CREATED");
        System.out.println("    Job ID       : " + job.getId());
        System.out.println("    Job Name     : " + job.getName());
        System.out.println("    Priority     : " + job.getPriority());
        System.out.println("    Job Status   : " + job.getStatus());

        assertNotNull(job.getId());
        assertEquals(JobStatus.CREATED, job.getStatus());

        // ------------------------------------------------
        // 2. QUEUE JOB
        // ------------------------------------------------

        System.out.println();
        System.out.println("[2] QUEUING JOB...");

        Execution execution =
                jobExecutionService.queueJob(job.getId());

        System.out.println("    Execution ID : " + execution.getId());
        System.out.println("    Exec Status  : " + execution.getStatus());

        assertNotNull(execution.getId());
        assertEquals(
                ExecutionStatus.QUEUED,
                execution.getStatus()
        );

        // ------------------------------------------------
        // 3. VERIFY DATABASE STATE
        // ------------------------------------------------

        System.out.println();
        System.out.println("[3] VERIFYING DATABASE STATE...");

        Job queuedJob =
                jobRepository.findById(job.getId())
                        .orElseThrow();

        Execution queuedExecution =
                executionRepository
                        .findById(execution.getId())
                        .orElseThrow();

        System.out.println(
                "    Job Status       : "
                        + queuedJob.getStatus()
        );

        System.out.println(
                "    Execution Status : "
                        + queuedExecution.getStatus()
        );

        assertEquals(
                JobStatus.QUEUED,
                queuedJob.getStatus()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                queuedExecution.getStatus()
        );

        // ------------------------------------------------
        // 4. WAIT FOR SCHEDULER + WORKER
        // ------------------------------------------------

        System.out.println();
        System.out.println(
                "[4] WAITING FOR SCHEDULER + WORKER..."
        );

        ExecutionStatus finalStatus = null;

        for (int i = 0; i < 20; i++) {

            TimeUnit.MILLISECONDS.sleep(250);

            finalStatus =
                    executionRepository
                            .findById(execution.getId())
                            .map(Execution::getStatus)
                            .orElse(null);

            System.out.println(
                    "    Poll " + (i + 1)
                            + " -> Execution Status: "
                            + finalStatus
            );

            if (finalStatus == ExecutionStatus.COMPLETED
                    || finalStatus == ExecutionStatus.FAILED) {

                break;
            }
        }

        // ------------------------------------------------
        // 5. FINAL STATE
        // ------------------------------------------------

        System.out.println();
        System.out.println("[5] FINAL DATABASE STATE");

        Job finalJob =
                jobRepository.findById(job.getId())
                        .orElseThrow();

        Execution finalExecution =
                executionRepository
                        .findById(execution.getId())
                        .orElseThrow();

        System.out.println(
                "    Job ID           : "
                        + finalJob.getId()
        );

        System.out.println(
                "    Job Status       : "
                        + finalJob.getStatus()
        );

        System.out.println(
                "    Execution ID     : "
                        + finalExecution.getId()
        );

        System.out.println(
                "    Execution Status : "
                        + finalExecution.getStatus()
        );

        System.out.println(
                "    Started At       : "
                        + finalExecution.getStartedAt()
        );

        System.out.println(
                "    Finished At      : "
                        + finalExecution.getFinishedAt()
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println("        PIPELINE TEST COMPLETE");
        System.out.println("========================================");

        assertEquals(
                ExecutionStatus.COMPLETED,
                finalExecution.getStatus()
        );

        assertEquals(
                JobStatus.COMPLETED,
                finalJob.getStatus()
        );
    }
}