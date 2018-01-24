package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OfficeIdentifiableTest {

    @Test
    public void defaults() {
        OfficeIdentifiable pi = new OfficeIdentifiable() {
            // Empty.
        };

        assertThat(pi.getOfficeId(), equalTo(0));
    }
}
