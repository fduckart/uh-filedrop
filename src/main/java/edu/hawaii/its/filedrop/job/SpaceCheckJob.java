package edu.hawaii.its.filedrop.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import edu.hawaii.its.filedrop.service.SpaceCheckService;

public class SpaceCheckJob implements Job {

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        spaceCheckService.update();
    }
}
