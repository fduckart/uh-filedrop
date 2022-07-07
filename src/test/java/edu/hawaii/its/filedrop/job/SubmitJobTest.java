package edu.hawaii.its.filedrop.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SubmitJobTest {

    @Test
    public void runTest() {
        SubmitJob submitJob = new AllowlistCheckJob();
        assertEquals("W", submitJob.getClassCode());

        submitJob = new SpaceCheckJob();
        assertEquals("S", submitJob.getClassCode());

        submitJob = new ExpireJob();
        assertEquals("E", submitJob.getClassCode());
    }
}
