package edu.hawaii.its.filedrop.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.job.JobFactory;
import edu.hawaii.its.filedrop.job.SubmitJob;

@Service
public class SchedulerService {

    @Autowired
    private JobFactory jobFactory;

    @Autowired
    private Scheduler scheduler;

    private static final Log logger = LogFactory.getLog(SchedulerService.class);

    @PostConstruct
    public void init() throws SchedulerException {
        for (SubmitJob submitJob : jobFactory.getJobs()) {
            addJob(submitJob);
        }
    }

    public JobDetail addJob(SubmitJob submitJob, String group) throws SchedulerException {
        JobDetail job;
        job = newJob(submitJob.getClass())
                .withIdentity(submitJob.getClass().getSimpleName(), group)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(job.getKey() + "Trigger")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(submitJob.getInterval())
                        .repeatForever())
                .build();
        scheduler.scheduleJob(job, trigger);
        logger.debug("Job added: " + job.toString());
        return job;
    }

    public JobDetail addJob(SubmitJob submitJob) throws SchedulerException {
        return addJob(submitJob, JobKey.DEFAULT_GROUP);
    }

    public boolean deleteJob(JobKey jobKey) throws SchedulerException {
        return scheduler.deleteJob(jobKey);
    }

    public JobDetail findJob(String submitJob) throws SchedulerException {
        return findJob(submitJob, JobKey.DEFAULT_GROUP);
    }

    public JobDetail findJob(String submitJob, String group) throws SchedulerException {
        return scheduler.getJobDetail(JobKey.jobKey(submitJob, group));
    }

}
