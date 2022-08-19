package edu.hawaii.its.filedrop.configuration;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class Realm {

    private static final Log logger = LogFactory.getLog(Realm.class);
    private final Map<String, Boolean> profileMap = new ConcurrentHashMap<>();

    @Autowired
    private Environment environment;

    @PostConstruct
    public synchronized void init() {
        logger.info("init; starting");
        logger.info("init; environment: " + environment);
        Assert.notNull(environment, "Property 'environment' is required.");
        for (String p : environment.getActiveProfiles()) {
            profileMap.put(p, Boolean.TRUE);
        }
        logger.info("init; finished");
    }

    public Boolean isProduction() {
        return isProfileActive("prod");
    }

    public Boolean isTest() {
        return isProfileActive("test");
    }

    public Boolean isDev() {
        return isProfileActive("dev");
    }

    public boolean isProfileActive(String profile) {
        for (String p : environment.getActiveProfiles()) {
            if (p.equals(profile)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyProfileActive(String... profiles) {
        if (profiles != null && profiles.length > 0) {
            for (String p : profiles) {
                if (p != null && profileMap.containsKey(p)) {
                    return true;
                }
            }
        }
        return false;
    }

}
