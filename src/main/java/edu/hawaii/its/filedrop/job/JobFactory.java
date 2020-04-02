package edu.hawaii.its.filedrop.job;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(
        basePackages = "edu.hawaii.its.filedrop.job",
        includeFilters = @ComponentScan.Filter(SubmitJobComponent.class))
public class JobFactory {

    private static final Log logger = LogFactory.getLog(JobFactory.class);

    @Autowired
    private List<SubmitJob> jobs;

    @PostConstruct
    public void init() {
        logger.info("init; starting...");

        synchronized (this) {
            for (SubmitJob job : jobs) {
                logger.info("init; Job: " + job);
            }
        }

        logger.info("init; started.");
    }

    public List<SubmitJob> getJobs() {
        return jobs;
    }
}
