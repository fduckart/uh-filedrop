package edu.hawaii.its.filedrop.service.mail;

import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class MailComponentLocator {

    private static final Log logger = LogFactory.getLog(MailComponentLocator.class);

    @Autowired
    private Map<String, MailTemplate> mails;

    @PostConstruct
    public void init() {
        logger.info("init; starting...");

        for (String key : mails.keySet()) {
            logger.info("init; MailTemplate(key=" + key + "): " + mails.get(key));
        }

        Assert.notNull(mails.get("empty"), "EmptyMail couldn't be loaded.");

        logger.info("init; finished.");
    }

    public MailTemplate find(String key) {
        return mails.getOrDefault(key, mails.get("empty"));
    }

    public Map<String, MailTemplate> getMails() {
        return mails;
    }

}
