package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("allowlist")
public class AllowlistMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "FileDrop Allowlist";
    }

    @Override
    public String toString() {
        return "AllowlistMail [subject=" + getSubject() + "]";
    }
}
