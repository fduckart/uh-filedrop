package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import edu.hawaii.its.filedrop.service.SpaceCheckService;

@SubmitJobComponent(classCode = "S")
@ConditionalOnProperty(
        prefix = "app.job.spacecheck",
        name = "enabled",
        matchIfMissing = true)
public class SpaceCheckJob extends SubmitJob {

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        spaceCheckService.update();
    }

    @Override
    @Value("${app.job.spacecheck.interval}")
    public void setInterval(int interval) {
        super.setInterval(interval);
    }
}
