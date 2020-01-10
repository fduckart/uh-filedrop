package edu.hawaii.its.filedrop.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.MessageService;
import edu.hawaii.its.filedrop.service.SpaceCheckService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.Message;
import edu.hawaii.its.filedrop.util.Files;

@Controller
public class HomeController {

    private static final Log logger = LogFactory.getLog(HomeController.class);

    @Value("${app.max.size:999}")
    private Integer maxSize;

    @Value("${cas.login.url}")
    private String casUrlLogin;

    @Value("${cas.send.renew:true}")
    private Boolean isCasSendRenew;

    @Value("${url.base}")
    private String urlBase;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private SpaceCheckService spaceCheckService;

    @GetMapping(value = { "/", "/home" })
    public String home(Model model) {
        logger.debug("User at home.");

        boolean spaceFull = !spaceCheckService.isFreeSpaceAvailable();
        int messageId = spaceFull ? Message.UNAVAILABLE_MESSAGE : Message.GATE_MESSAGE;
        Message message = messageService.findMessage(messageId);
        model.addAttribute("gatemessage", message.getText());


        return "home";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/login")
    public String login() {
        logger.debug("User at login.");
        return "redirect:/user";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/data")
    public String userData() {
        logger.debug("User at user/data.");
        Mail mail = new Mail();
        mail.setFrom(emailService.getFrom());
        mail.setTo(currentUser().getAttribute("uhEmail"));
        mail.setSubject("Testing from Spring Boot");
        mail.setContent("Test from the UH FileDrop Application."
                + "\n\nYour basic User information:\n" + currentUser());
        emailService.send(mail);
        return "redirect:/user";
    }

    @GetMapping(value = { "/campus", "/campuses" })
    public String campus() {
        logger.debug("User at campus.");
        return "campus";
    }

    @GetMapping(value = { "/help/contact", "/help/contacts" })
    public String contact() {
        logger.debug("User at contact.");
        return "help/contact";
    }

    @GetMapping(value = { "/help/faq", "/help/faqs" })
    public String faq(Model model) {
        logger.debug("User at faq.");
        model.addAttribute("maxSize", FileUtils.byteCountToDisplaySize(maxSize));
        return "help/faq";
    }

    @GetMapping(value = "/help/fonts")
    public String fonts() {
        logger.debug("User at fonts.");
        return "help/fonts";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping("/user")
    public String adminUser(Model model) {
        logger.debug("User at /user.");

        model.addAttribute("user", currentUser());

        return "user/user";
    }

    @GetMapping(value = "/404")
    public String invalid() {
        return "redirect:/";
    }

    public EmailService getEmailService() {
        return emailService;
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }

    @ModelAttribute("maxSize")
    public String maxSizeDisplay() {
        return Files.byteCountToDisplaySize(maxSize);
    }
}
