package edu.hawaii.its.filedrop.service.mail;

import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailComponentLocator {

    private static final Log logger = LogFactory.getLog(MailComponentLocator.class);

    @Autowired
    private Map<String, MailTemplate> mails;

    @PostConstruct
    public void init() {
        logger.info("init; starting...");

        for (String key : mails.keySet()) {
            logger.info("init; MailTemplate(key=" + key + "): " + mails.get(key).getClass().getCanonicalName());
        }

        logger.info("init; finished.");
    }

    public MailTemplate find(String key) {
        return mails.get(key);
    }

    public Map<String, MailTemplate> getMails() {
        return mails;
    }

}
