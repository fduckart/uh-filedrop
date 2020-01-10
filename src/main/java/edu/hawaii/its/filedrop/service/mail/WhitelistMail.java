package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("whitelist")
public class WhitelistMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "FileDrop Whitelist";
    }
}
