package edu.hawaii.its.filedrop.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import edu.hawaii.its.filedrop.service.FileDropService;

public class ExpireJob extends SubmitJob {

    @Autowired
    private FileDropService fileDropService;

    @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    }
}
