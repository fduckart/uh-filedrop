package edu.hawaii.its.filedrop.controller;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.EmailService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.MessageService;
import edu.hawaii.its.filedrop.service.SpaceCheckService;
import edu.hawaii.its.filedrop.type.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private FileDropService fileDropService;

    @GetMapping(value = { "/", "/home" })
    public String home(Model model) {
        logger.debug("User at home.");

        model.addAttribute("maxSize", maxSize);
        model.addAttribute("urlBase", urlBase);
        model.addAttribute("casUrlLogin", casUrlLogin);
        model.addAttribute("isCasRenew", isCasSendRenew);

        boolean spaceFull = !spaceCheckService.isFreeSpaceAvailable();
        model.addAttribute("spaceFull", spaceFull);

        int messageId = spaceFull ? Message.UNAVAILABLE_MESSAGE : Message.GATE_MESSAGE;
        Message message = messageService.findMessage(messageId);
        model.addAttribute("gatemessage", message.getText());

        return "home";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = { "/prepare" })
    public String prepare(Model model, @RequestParam(value = "helpdesk", required = false) Boolean helpdesk) {
        logger.debug("User at prepare.");
        fileDropService.startUploadProcess(userContextService.getCurrentUser());
        model.addAttribute("user", userContextService.getCurrentUser().getUsername() + "@hawaii.edu");
        model.addAttribute("helpdesk", helpdesk);
        return "user/prepare";
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
        emailService.send(userContextService.getCurrentUser());
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
        model.addAttribute("maxSize", maxSize);
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

        User user = userContextService.getCurrentUser();
        model.addAttribute("user", user);

        return "user/user";
    }

    @GetMapping(value = "/404")
    public String invalid() {
        return "redirect:/";
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

}
