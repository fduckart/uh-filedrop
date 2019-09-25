package edu.hawaii.its.filedrop.job;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubmitJobTest {

    @Test
    public void runTest() {
        SubmitJob submitJob = new WhitelistCheckJob();
        assertEquals("W", submitJob.getClassCode());

        submitJob = new SpaceCheckJob();
        assertEquals("Z", submitJob.getClassCode());
    }
}
