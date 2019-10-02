package edu.hawaii.its.filedrop.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.job.WhitelistCheckJob;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SchedulerServiceTest {

    @Autowired
    private SchedulerService schedulerService;

    @Test
    public void findJobTest() throws SchedulerException {
        JobDetail jobDetail = schedulerService.findJob("WhitelistCheckJob");

        assertNotNull(jobDetail);

        jobDetail = schedulerService.findJob("NullJob");
        assertNull(jobDetail);

        jobDetail = schedulerService.findJob("NullJob", "NULL");
        assertNull(jobDetail);
    }

    @Test
    public void addJobTest() throws SchedulerException {
        WhitelistCheckJob whitelistCheckJob = new WhitelistCheckJob();
        whitelistCheckJob.setInterval(10);
        JobDetail jobDetail = schedulerService.addJob(whitelistCheckJob, "TEST");

        assertNotNull(jobDetail);

        jobDetail = schedulerService.findJob("WhitelistCheckJob", "TEST");
        assertNotNull(jobDetail);
    }

    @Test
    public void deleteJobTest() throws SchedulerException {
        WhitelistCheckJob whitelistCheckJob = new WhitelistCheckJob();
        whitelistCheckJob.setInterval(10);

        JobDetail jobDetail = schedulerService.addJob(whitelistCheckJob, "TEST");
        assertNotNull(jobDetail);

        boolean deleted = schedulerService.deleteJob(schedulerService.findJob("WhitelistCheckJob", "TEST").getKey());
        jobDetail = schedulerService.findJob("WhitelistCheckJob", "TEST");
        assertTrue(deleted);
        assertNull(jobDetail);
    }

    @Test(expected = NullPointerException.class)
    public void nullJob() throws SchedulerException {
        JobDetail jobDetail = schedulerService.addJob(null);
    }
}
