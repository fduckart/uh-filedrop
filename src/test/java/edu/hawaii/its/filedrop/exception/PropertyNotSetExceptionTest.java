package edu.hawaii.its.filedrop.exception;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

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

        propertyNotSetException = new PropertyNotSetException("property", "test", "value");
        assertNotNull(propertyNotSetException);
        assertThat(propertyNotSetException.getMessage(), containsString("Property (property)"));
        assertThat(propertyNotSetException.getMessage(), containsString("expected: 'test'"));
        assertThat(propertyNotSetException.getMessage(), containsString("actual: 'value'"));
    }
}
