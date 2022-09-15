package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTest {

    private Message message;

    @BeforeEach
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
        assertThat(message.isEnabled(), equalTo(true));
        assertThat(message.getEnabled(), equalTo(true));
        assertThat(message.getText(), equalTo(""));
        assertNull(message.getTypeId());

        message.setId(666);
        assertThat(message.getId(), equalTo(666));
    }

    @Test
    public void testToString() {
        String expected = "Message [id=null, typeId=null, enabled=true, text=]";
        assertThat(message.toString(), containsString(expected));

        message.setId(12345);
        assertThat(message.toString(), containsString("Message [id=12345,"));
    }

    @Test
    public void testEquals() {
        Message c0 = new Message();
        assertThat(c0, equalTo(c0));
        assertThat(c0, not(equalTo("")));
        assertNotEquals(null, c0);
        assertNotEquals(c0, null);
        Message c1 = new Message();
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));

        c0.setId(1);
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        c1.setId(1);
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
    }

    @Test
    public void testHash() {
        Message message = new Message();
        message.setText("Test");
        message.setId(123456);
        message.setTypeId(1);

        Message message2 = new Message();
        message.setText("Test");
        message.setId(123456);
        message.setTypeId(1);

        assertNotEquals(message2.hashCode(), message.hashCode());
    }
}
