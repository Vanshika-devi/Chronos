import com.chronos.model.Job;
import com.chronos.model.JobStatus;
import com.chronos.scheduler.JobQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueTest {

    @Test
    void shouldReturnJobsInPriorityOrder() {

        // Create jobs
        Job jobA = new Job(
                null,
                "Job-A",
                2,
                JobStatus.CREATED
        );

        Job jobB = new Job(
                null,
                "Job-B",
                10,
                JobStatus.CREATED
        );

        Job jobC = new Job(
                null,
                "Job-C",
                5,
                JobStatus.CREATED
        );

        Job jobD = new Job(
                null,
                "Job-D",
                1,
                JobStatus.CREATED
        );

        // Create queue
        JobQueue queue = new JobQueue();

        // Add jobs
        queue.add(jobA);
        queue.add(jobB);
        queue.add(jobC);
        queue.add(jobD);

        // Highest priority should come first
        assertEquals("Job-B", queue.poll().getName());
        assertEquals("Job-C", queue.poll().getName());
        assertEquals("Job-A", queue.poll().getName());
        assertEquals("Job-D", queue.poll().getName());

        // Queue should now be empty
        assertTrue(queue.isEmpty());
    }
}