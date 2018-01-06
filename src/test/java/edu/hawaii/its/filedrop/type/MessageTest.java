package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MessageTest {

    private Message message;

    @Before
    public void setUp() {
        message = new Message();
    }

    @Test
    public void construction() {
        assertNotNull(message);
    }

    @Test
    public void accessors() {
        assertNotNull(message);
        assertNull(message.getId());
        assertNull(message.getEnabled());
        assertThat(message.getText(), equalTo(""));
        assertNull(message.getTypeId());

        message.setId(666);
        assertThat(message.getId(), equalTo(666));
    }

    @Test
    public void testToString() {
        String expected = "Message [id=null, typeId=null, enabled=null, text=]";
        assertThat(message.toString(), containsString(expected));

        message.setId(12345);
        assertThat(message.toString(), containsString("Message [id=12345,"));
    }
}
