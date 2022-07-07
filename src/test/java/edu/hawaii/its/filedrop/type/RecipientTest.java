package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecipientTest {

    Recipient recipient;

    @BeforeEach
    public void setUp() {
        recipient = new Recipient();
    }

    @Test
    public void accessors() {
        assertThat(recipient.getId(), equalTo(null));
        assertThat(recipient.getName(), equalTo(null));
        assertThat(recipient.getFileDrop(), equalTo(null));

        recipient.setId(666);
        recipient.setName("TB");
        assertThat(recipient.getId(), equalTo(666));
        assertThat(recipient.getName(), equalTo("TB"));

        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(123);
        recipient.setFileDrop(fileDrop);
        assertThat(recipient.getFileDrop(), not(equalTo(null)));
        assertThat(recipient.getFileDrop().getId(), equalTo(123));
    }

    @Test
    public void testToString() {
        assertThat(recipient.toString(), startsWith("Recipient ["));
        assertThat(recipient.toString(), containsString("id=null, "));
        assertThat(recipient.toString(), containsString(", name=null"));
        assertThat(recipient.toString(), containsString(", fileDrop.id=null"));

        recipient.setId(666);
        assertThat(recipient.toString(), startsWith("Recipient [id=666"));

        recipient.setName("EX");
        assertThat(recipient.toString(), containsString(", name=EX"));

        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(321);
        recipient.setFileDrop(fileDrop);
        assertThat(recipient.toString(), containsString(", fileDrop.id=321"));
    }
}
