package edu.hawaii.its.filedrop.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void findMessages() {
        Message m1 = messageRepository.findById(1).get();
        Message m2 = messageRepository.findById(2).get();

        assertEquals("<span style=\"color: red;\"><strong>WARNING: Not intended for normal use (test-env)</strong></span><br/>Welcome to the University of Hawai'i FileDrop application.", m1.getText());
        assertEquals("Welcome to the University of Hawai'i FileDrop application.", m2.getText());

        m2 = m1;

        assertSame(m1, m2);

        m1.setText("Test");
        messageRepository.save(m1);

        assertEquals("Test", m1.getText());
    }

}
