package edu.hawaii.its.filedrop.exception;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PropertyNotSetExceptionTest {

    @Test
    public void construction() {
        PropertyNotSetException propertyNotSetException = new PropertyNotSetException("test");
        assertNotNull(propertyNotSetException);
        assertThat(propertyNotSetException.getMessage(), equalTo("test"));

        propertyNotSetException = new PropertyNotSetException("test", new Throwable("message"));
        assertNotNull(propertyNotSetException);
        assertThat(propertyNotSetException.getMessage(), containsString("test"));
        assertThat(propertyNotSetException.getCause().getMessage(), equalTo("message"));
    }
}
