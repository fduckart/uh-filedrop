package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import edu.hawaii.its.filedrop.service.FileDropService;

@SubmitJobComponent(classCode = "E")
@ConditionalOnProperty(
    prefix = "app.job.expire",
    name = "enabled",
    matchIfMissing = true)
public class ExpireJob extends SubmitJob {

    @Autowired
    private FileDropService fileDropService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        fileDropService.checkFileDrops();
    }

    @Override
    @Value("${app.job.expire.interval}")
    public void setInterval(int interval) {
        super.setInterval(interval);
    }
}
