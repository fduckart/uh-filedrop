package edu.hawaii.its.filedrop.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AppConfig {
    protected final Log logger = LogFactory.getLog(getClass());

    abstract void init();
}
