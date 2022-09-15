package edu.hawaii.its.filedrop.exception;

public class FileDropNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileDropNotFoundException(String message) {
        super(message);
    }

    public FileDropNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
