package edu.hawaii.its.filedrop.exception;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class StorageExceptionTest {

    @Test
    public void construction() {
        StorageException exception = new StorageException("fail");
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo("fail"));

        exception = new StorageException("stop", new Throwable("me"));
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo("stop"));
        assertThat(exception.getLocalizedMessage(), equalTo("stop"));

        exception = new StorageException(new Throwable("me"));
        assertNotNull(exception);
        assertThat(exception.getCause().getMessage(), equalTo("me"));
        assertThat(exception.getCause().getLocalizedMessage(), equalTo("me"));
    }
}
