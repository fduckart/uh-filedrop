package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.hawaii.its.filedrop.service.SpaceCheckService;

@SubmitJobComponent
public class SpaceCheckJob extends SubmitJob {

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        spaceCheckService.update();
    }

    @Override
    @Value("${app.scheduler.spacecheck.interval}")
    public void setInterval(int interval) {
        super.setInterval(interval);
    }
}
