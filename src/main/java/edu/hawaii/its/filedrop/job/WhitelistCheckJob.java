package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.hawaii.its.filedrop.service.WhitelistService;

@SubmitJobComponent(classCode = "W")
public class WhitelistCheckJob extends SubmitJob {

    @Autowired
    private WhitelistService whitelistService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        whitelistService.checkWhitelists();
    }

    @Override
    @Value("${app.scheduler.whitelistcheck.interval}")
    public void setInterval(int interval) {
        super.setInterval(interval);
    }
}
