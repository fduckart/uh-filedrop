package edu.hawaii.its.filedrop.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Message;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void findMessages() {
        Message m1 = messageRepository.findById(2).get();
        Message m2 = messageRepository.findById(2).get();
        assertThat(m1, equalTo(m2));
        assertThat(m1.getText(), startsWith("Welcome to the University of"));

        m2 = m1;
        assertSame(m1, m2);

        m1.setText("Test");
        messageRepository.save(m1);

        assertThat(m1.getText(), equalTo("Test"));
    }

}
