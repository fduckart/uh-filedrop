package edu.hawaii.its.filedrop.configuration.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import edu.hawaii.its.filedrop.exception.PropertyNotSetException;

public class PropertyNotSetAnalyzer extends AbstractFailureAnalyzer<PropertyNotSetException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, PropertyNotSetException cause) {

        String description = "Component initialization error. \n"
            + "Property does not match expected value:\n"
            + cause.getMessage();

        String action = "Set correct property value for current profile.";
        return new FailureAnalysis(description, action, cause);
    }

}
