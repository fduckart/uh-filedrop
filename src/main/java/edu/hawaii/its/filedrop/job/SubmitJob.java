package edu.hawaii.its.filedrop.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;

@SubmitJobComponent
public abstract class SubmitJob implements Job {

    protected final Log logger = LogFactory.getLog(getClass());
    private final String classCode;

    protected SubmitJob() {
        Class<SubmitJobComponent> clazz = SubmitJobComponent.class;
        SubmitJobComponent jobAnnotation = getClass().getAnnotation(clazz);
        if (jobAnnotation == null) {
            throw new RuntimeException("Annotation error");
        }
        classCode = jobAnnotation.classCode();
    }

    public String getClassCode() {
        return classCode;
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName() + " ["
                + "classCode=" + classCode
                + "]";
    }
}
