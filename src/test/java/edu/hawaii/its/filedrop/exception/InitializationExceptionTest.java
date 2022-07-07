package edu.hawaii.its.filedrop.exception;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class InitializationExceptionTest {

    @Test
    public void construction() {
        InitializationException initializationException = new InitializationException("test message");
        assertNotNull(initializationException);
        assertThat(initializationException.getMessage(), equalTo("test message"));

        initializationException = new InitializationException("test", new Throwable("message"));
        assertNotNull(initializationException);
        assertThat(initializationException.getMessage(), containsString("test"));
        assertThat(initializationException.getCause().getMessage(), equalTo("message"));
    }
}
