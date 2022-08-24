package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import edu.hawaii.its.filedrop.service.AllowlistService;

@SubmitJobComponent(classCode = "W")
@ConditionalOnProperty(
        prefix = "app.job.allowlist",
        name = "enabled",
        matchIfMissing = true)
public class AllowlistCheckJob extends SubmitJob {

    @Autowired
    private AllowlistService allowlistService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        allowlistService.checkAllowlists();
    }

    @Override
    @Value("${app.job.allowlist.interval}")
    public void setInterval(int interval) {
        super.setInterval(interval);
    }
}
