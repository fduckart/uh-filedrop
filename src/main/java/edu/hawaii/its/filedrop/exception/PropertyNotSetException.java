package edu.hawaii.its.filedrop.exception;

public class PropertyNotSetException extends InitializationException {

    public PropertyNotSetException(String property, String expected, String value) {
        super("\tProperty (" + property + "): \nexpected: '" + expected + "'\n\tactual: '" + value + "'");
    }

    public PropertyNotSetException(String message) {
        super(message);
    }

    public PropertyNotSetException(String message, Throwable cause) {
        super(message, cause);
    }
}
