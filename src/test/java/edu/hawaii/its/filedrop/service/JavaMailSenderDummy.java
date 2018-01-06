package edu.hawaii.its.filedrop.service;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class JavaMailSenderDummy implements JavaMailSender {

    @Override
    public void send(SimpleMailMessage arg0) throws MailException {
        // Empty.
    }

    @Override
    public void send(SimpleMailMessage... arg0) throws MailException {
        // Empty.
    }

    @Override
    public MimeMessage createMimeMessage() {
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream arg0) throws MailException {
        return null;
    }

    @Override
    public void send(MimeMessage arg0) throws MailException {
        // Empty.
    }

    @Override
    public void send(MimeMessage... arg0) throws MailException {
        // Empty.
    }

    @Override
    public void send(MimeMessagePreparator arg0) throws MailException {
        // Empty.
    }

    @Override
    public void send(MimeMessagePreparator... arg0) throws MailException {
        // Empty.
    }

}
