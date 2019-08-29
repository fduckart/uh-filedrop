package edu.hawaii.its.filedrop.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import edu.hawaii.its.filedrop.service.WhitelistService;

public class WhitelistCheckJob implements Job {

    @Autowired
    private WhitelistService whitelistService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    }
}
