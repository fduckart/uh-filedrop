package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import edu.hawaii.its.filedrop.service.WhitelistService;

@SubmitJobComponent(classCode = "W")
@ConditionalOnProperty(
        prefix = "app.job.whitelist",
        name = "enabled",
        matchIfMissing = true)
public class WhitelistCheckJob extends SubmitJob {

    @Autowired
    private WhitelistService whitelistService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        whitelistService.checkWhitelists();
    }

    @Override
    @Value("${app.job.whitelist.interval}")
    public void setInterval(int interval) {
        super.setInterval(interval);
    }
}
