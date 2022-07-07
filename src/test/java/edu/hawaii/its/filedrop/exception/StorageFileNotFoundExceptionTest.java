package edu.hawaii.its.filedrop.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class StorageFileNotFoundExceptionTest {

    @Test
    public void construction() {
        StorageFileNotFoundException ex = new StorageFileNotFoundException("fail");
        assertNotNull(ex);
        assertThat(ex.getMessage(), equalTo("fail"));

        ex = new StorageFileNotFoundException("stop", new Throwable("me"));
        assertNotNull(ex);
        assertThat(ex.getMessage(), equalTo("stop"));
        assertThat(ex.getLocalizedMessage(), equalTo("stop"));
    }
}
