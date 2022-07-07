package edu.hawaii.its.filedrop.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.job.AllowlistCheckJob;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SchedulerServiceTest {

    @Autowired
    private SchedulerService schedulerService;

    @Test
    public void findJobTest() throws SchedulerException {
        JobDetail jobDetail = schedulerService.findJob("AllowlistCheckJob");

        assertNotNull(jobDetail);

        jobDetail = schedulerService.findJob("NullJob");
        assertNull(jobDetail);

        jobDetail = schedulerService.findJob("NullJob", "NULL");
        assertNull(jobDetail);
    }

    @Test
    public void addJobTest() throws SchedulerException {
        AllowlistCheckJob allowlistCheckJob = new AllowlistCheckJob();
        allowlistCheckJob.setInterval(10);
        JobDetail jobDetail = schedulerService.addJob(allowlistCheckJob, "TEST");

        assertNotNull(jobDetail);

        jobDetail = schedulerService.findJob("AllowlistCheckJob", "TEST");
        assertNotNull(jobDetail);
    }

    @Test
    public void deleteJobTest() throws SchedulerException {
        AllowlistCheckJob allowlistCheckJob = new AllowlistCheckJob();
        allowlistCheckJob.setInterval(10);

        JobDetail jobDetail = schedulerService.addJob(allowlistCheckJob, "TEST");
        assertNotNull(jobDetail);

        boolean deleted = schedulerService.deleteJob(schedulerService.findJob("AllowlistCheckJob", "TEST").getKey());
        jobDetail = schedulerService.findJob("AllowlistCheckJob", "TEST");
        assertTrue(deleted);
        assertNull(jobDetail);
    }

    @Test
    public void nullJob() throws SchedulerException {
        assertThrows(NullPointerException.class,
                () -> schedulerService.addJob(null));
    }
}
