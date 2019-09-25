package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import edu.hawaii.its.filedrop.service.WhitelistService;

@SubmitJobComponent(classCode = "W")
public class WhitelistCheckJob extends SubmitJob {

    @Autowired
    private WhitelistService whitelistService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        whitelistService.checkWhitelists();
    }
}
