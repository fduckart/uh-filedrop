package edu.hawaii.its.filedrop.exception;

public class StorageFileNotFoundException extends StorageException {

    private static final long serialVersionUID = 26L;

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
