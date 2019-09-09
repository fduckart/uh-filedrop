package edu.hawaii.its.filedrop.service;

import java.time.LocalDate;
import java.util.List;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.job.WhitelistCheckJob;
import edu.hawaii.its.filedrop.repository.WhitelistRepository;
import edu.hawaii.its.filedrop.type.Whitelist;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class WhitelistService {

    @Autowired
    private WhitelistRepository whitelistRepository;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private Scheduler scheduler;

    @Value("${app.scheduler.whitelistcheck.interval}")
    private int interval;

    @Value("${app.whitelist.check.threshold}")
    private int threshold;

    private JobKey jobKey;

    private static final Log logger = LogFactory.getLog(WhitelistService.class);

    @PostConstruct
    public void init() throws SchedulerException {
        JobDetail whitelistCheckJob = newJob(WhitelistCheckJob.class)
                .withIdentity("whitelistCheck")
                .build();

        Trigger whitelistCheckTrigger = newTrigger()
                .withIdentity("whitelistCheckTrigger")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(interval)
                        .repeatForever())
                .build();
        jobKey = whitelistCheckJob.getKey();
        scheduler.scheduleJob(whitelistCheckJob, whitelistCheckTrigger);
    }

    public Whitelist addWhitelist(LdapPerson entry, LdapPerson registrant) {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry(entry.getUid());
        whitelist.setRegistrant(registrant.getUid());
        whitelist.setCheck(0);
        whitelist.setExpired(false);
        whitelist.setCreated(LocalDate.now());
        return addWhitelist(whitelist);
    }

    public Whitelist addWhitelist(Whitelist whitelist) {
        return whitelistRepository.save(whitelist);
    }

    public int addCheck(Whitelist whitelist, int amount) {
        whitelist.setCheck(whitelist.getCheck() + amount);
        if (whitelist.getCheck() >= threshold) {
            whitelist.setExpired(true);
        }
        addWhitelist(whitelist);
        return whitelist.getCheck();
    }

    public synchronized void checkWhitelists() {
        logger.debug("Starting whitelist check...");
        for (Whitelist whitelist : getAllWhiteList()) {
            if (ldapService.findByUhUuidOrUidOrMail(whitelist.getRegistrant()) instanceof LdapPersonEmpty) {
                addCheck(whitelist, 1);
            }
        }
        logger.debug("Finished whitelist check.");
    }

    public Whitelist getWhiteList(Integer id) {
        Whitelist whitelist = whitelistRepository.findById(id).orElse(null);
        if (whitelist != null) {
            whitelist.setEntryName(ldapService.findByUid(whitelist.getEntry()).getCn());
            whitelist.setRegistrantName(ldapService.findByUid(whitelist.getRegistrant()).getCn());
        }
        return whitelist;
    }

    public List<Whitelist> getAllWhiteList() {
        List<Whitelist> whitelists = whitelistRepository.findAll();
        whitelists.forEach(whitelist -> {
            whitelist.setEntryName(ldapService.findByUhUuidOrUidOrMail(whitelist.getEntry()).getCn());
            whitelist.setRegistrantName(ldapService.findByUhUuidOrUidOrMail(whitelist.getRegistrant()).getCn());
        });
        return whitelists;
    }

    public boolean isWhitelisted(String entry) {
        return whitelistRepository.findByEntry(entry) != null;
    }

    public JobKey getJobKey() {
        return jobKey;
    }
}
