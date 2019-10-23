package edu.hawaii.its.filedrop.exception;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
