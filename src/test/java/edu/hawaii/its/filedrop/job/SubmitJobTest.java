package edu.hawaii.its.filedrop.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubmitJobTest {

    @Test
    public void runTest() {
        SubmitJob submitJob = new WhitelistCheckJob();
        assertEquals("W", submitJob.getClassCode());

        submitJob = new SpaceCheckJob();
        assertEquals("S", submitJob.getClassCode());

        submitJob = new ExpireJob();
        assertEquals("E", submitJob.getClassCode());
    }
}
