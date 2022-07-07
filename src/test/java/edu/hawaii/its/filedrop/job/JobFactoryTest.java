package edu.hawaii.its.filedrop.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JobFactoryTest {

    private JobFactory jobFactory;

    @BeforeEach
    public void setUp() {
        jobFactory = new JobFactory(Collections.emptyList());
    }

    @Test
    public void construction() {
        assertThat(jobFactory, not(equalTo(null)));
        assertThat(jobFactory.getJobs(), not(equalTo(null)));

        List<SubmitJob> jobs = new ArrayList<>();
        jobFactory = new JobFactory(jobs);
        assertThat(jobFactory.getJobs().size(), equalTo(0));

        jobs.add(new SpaceCheckJob());
        jobFactory = new JobFactory(jobs);
        assertThat(jobFactory.getJobs().size(), equalTo(1));

        jobFactory = new JobFactory(null);
        assertThat(jobFactory.getJobs(), not(equalTo(null)));
        assertThat(jobFactory.getJobs().size(), equalTo(0));
    }

}
