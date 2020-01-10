package edu.hawaii.its.filedrop.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.type.Mail;

@Service
public class EmailService {

    private static final Log logger = LogFactory.getLog(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${app.mail.enabled}")
    private boolean isEnabled;

    @Value("${app.mail.from:no-reply}")
    private String from;

    @Autowired
    private ITemplateEngine htmlTemplateEngine;

    // Constructor
    public EmailService() {
        // empty
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void send(Mail mail, String template, Context context) {
        logger.info("Sending email from send(mail, template, context)");
        if (isEnabled && mail.getFrom() != null && mail.getTo() != null) {
            String htmlContent = htmlTemplateEngine.process(template, context);

            MimeMessagePreparator msg = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setFrom(mail.getFrom());
                messageHelper.setTo(mail.getTo());
                messageHelper.setSubject(mail.getSubject());
                messageHelper.setText(htmlContent, true);
            };

            javaMailSender.send(msg);
        }
    }

    public void send(Mail mail) {
        logger.info("Sending email from send(user)...");
        if (isEnabled && mail.getTo() != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(mail.getTo());
            msg.setFrom(mail.getFrom());
            msg.setText(mail.getContent());
            msg.setSubject(mail.getSubject());
            try {
                javaMailSender.send(msg);
            } catch (MailException ex) {
                logger.error("Error", ex);
            }
        }
    }

    public String getFrom() {
        return from;
    }
}
