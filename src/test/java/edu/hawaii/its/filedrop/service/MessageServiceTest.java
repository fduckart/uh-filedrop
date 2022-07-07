package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Message;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Test
    public void findMessage() {
        Message message = messageService.findMessage(Message.GATE_MESSAGE);
        assertThat(message.getId(), equalTo(Message.GATE_MESSAGE));
        assertTrue(message.isEnabled());
        assertTrue(message.getText().contains("University of Hawai'i"));

        // Turn down logging just for a second, just
        // to reduce the Exception noise a little bit.
        Logger logger = (Logger) LoggerFactory.getLogger(MessageService.class);
        Level level = logger.getLevel();
        logger.setLevel(Level.OFF);

        // No matching ID, so empty object returned.
        message = messageService.findMessage(-1);
        assertNotNull(message);
        assertThat(message.getId(), equalTo(null));
        assertThat(message.isEnabled(), equalTo(null));
        assertThat(message.getTypeId(), equalTo(null));
        assertThat(message.getText(), equalTo(""));

        // Cause an internal exception to happen.
        message = messageService.findMessage(Message.UNAVAILABLE_MESSAGE);
        assertNotNull(message);

        // Make sure the denied access message actually exists.
        messageService.evictCache();
        message = messageService.findMessage(Message.UNAVAILABLE_MESSAGE);
        assertThat(message.getId(), equalTo(Message.UNAVAILABLE_MESSAGE));
        assertThat(message.getText(), containsString("unavailable"));

        // Put original logging level back.
        logger.setLevel(level);
    }

    @Test
    public void update() {
        Message message = messageService.findMessage(Message.GATE_MESSAGE);
        assertTrue(message.isEnabled());
        assertEquals(Integer.valueOf(1), message.getTypeId());
        assertTrue(message.getText().contains("University of Hawai'i"));
        assertTrue(message.getText().endsWith("."));

        final String text = message.getText();

        message.setText("Stemming the bleeding.");
        messageService.update(message);

        message = messageService.findMessage(Message.GATE_MESSAGE);
        assertTrue(message.isEnabled());
        assertEquals(Integer.valueOf(1), message.getTypeId());
        assertTrue(message.getText().equals("Stemming the bleeding."));

        // Put the original text back.
        message.setText(text);
        messageService.update(message);
        assertTrue(message.getText().contains("University of Hawai'i"));
        assertTrue(message.getText().endsWith("."));
    }

    @Test
    public void messageCache() {
        Message m0 = messageService.findMessage(Message.GATE_MESSAGE);
        Message m1 = messageService.findMessage(Message.GATE_MESSAGE);
        assertSame(m0, m1);

        m0.setText("This land is your land.");
        messageService.update(m0);
        assertSame(m0, m1);

        m1 = messageService.findMessage(Message.GATE_MESSAGE);
        assertSame(m0, m1);

        Message m2 = messageService.findMessage(Message.GATE_MESSAGE);
        assertSame(m0, m2);
        assertSame(m1, m2);

        Message m3 = new Message();
        m3.setId(999);
        m3.setEnabled(Boolean.TRUE);
        m3.setText("Testing");
        m3.setTypeId(1);
        messageService.add(m3);

        Message m4 = messageService.findMessage(999);
        assertEquals(m4, m3);
        assertEquals(m4, m3);
    }

}
