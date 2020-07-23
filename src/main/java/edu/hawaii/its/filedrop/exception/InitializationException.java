package edu.hawaii.its.filedrop.exception;

import org.springframework.beans.factory.BeanCreationException;

public class InitializationException extends BeanCreationException {

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
