package edu.hawaii.its.filedrop.exception;

public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 13L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
