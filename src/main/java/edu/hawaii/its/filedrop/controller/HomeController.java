package edu.hawaii.its.filedrop.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.ApplicationService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.MessageService;
import edu.hawaii.its.filedrop.service.SpaceCheckService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.Faq;
import edu.hawaii.its.filedrop.type.FileDropInfo;
import edu.hawaii.its.filedrop.type.Message;
import edu.hawaii.its.filedrop.type.Setting;
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
    private ApplicationService applicationService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Autowired
    private UserContextService userContextService;

    @GetMapping(value = { "/", "/home" })
    public String home(Model model) {
        logger.debug("User at home.");

        boolean spaceFull = !spaceCheckService.isFreeSpaceAvailable();
        logger.info("home; spaceFull: " + spaceFull);
        int messageId = spaceFull ? Message.UNAVAILABLE_MESSAGE : Message.GATE_MESSAGE;
        Message message = messageService.findMessage(messageId);
        model.addAttribute("gatemessage", message.getText());
        Setting disableLanding = applicationService.findSetting(1);
        if (Boolean.parseBoolean(disableLanding.getValue())) {
            model.addAttribute("gatemessage", "System is unavailable.");
        }
        model.addAttribute("disableLanding", Boolean.parseBoolean(disableLanding.getValue()));
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

    @GetMapping(value = { "/help" })
    public String help(Model model) {
        logger.debug("User at help.");
        model.addAttribute("maxSize", Files.byteCountToDisplaySize(maxSize));
        return "help/faq";
    }

    @GetMapping(value = { "/help/contact", "/help/contacts" })
    public String contact() {
        logger.debug("User at contact.");
        return "help/contact";
    }

    @GetMapping(value = { "/help/faq", "/help/faqs" })
    public String faq(Model model) {
        logger.debug("User at faq.");
        model.addAttribute("maxSize", Files.byteCountToDisplaySize(maxSize));
        return "help/faq";
    }

    @GetMapping(value = { "/help/permissions", "/help/permission", "/help/restrictions", "/help/restriction" })
    public String permissions() {
        logger.debug("User at help/permissions");
        return "help/permissions";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping("/user")
    public String adminUser(Model model) {
        logger.debug("User at /user.");

        model.addAttribute("user", currentUser());

        return "user/user";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("user", currentUser().getUsername());
        logger.debug("User at history");
        return "user/history";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/filedrops")
    public ResponseEntity<List<FileDropInfo>> findUserFileDrops() {
        logger.debug(currentUser().getUsername() + " looked for their FileDrops");

        List<FileDropInfo> fileDrops = fileDropService.findAllUserFileDropInfo(currentUser());

        return ResponseEntity.ok().body(fileDrops);
    }

    @GetMapping("/api/faq")
    public ResponseEntity<List<Faq>> getFaqs() {
        return ResponseEntity.ok().body(applicationService.findFaqs());
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
}
