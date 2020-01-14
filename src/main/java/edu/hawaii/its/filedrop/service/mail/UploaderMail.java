package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("uploader")
public class UploaderMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "Your files have been received by the UH FileDrop Service";
    }

    @Override
    public String toString() {
        return "UploaderMail [subject=" + getSubject() + "]";
    }
}
