package edu.hawaii.its.filedrop.exception;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class FileDropNotFoundExceptionTest {

    @Test
    public void construction() {
        FileDropNotFoundException exception = new FileDropNotFoundException("test");
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo("test"));

        exception = new FileDropNotFoundException("test", new Throwable("message"));
        assertNotNull(exception);
        assertThat(exception.getMessage(), containsString("test"));
        assertThat(exception.getCause().getMessage(), equalTo("message"));
    }
}
