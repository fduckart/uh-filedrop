package edu.hawaii.its.filedrop.controller;

import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PrepareController {

    private Log logger = LogFactory.getLog(PrepareController.class);

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private LdapService ldapService;

    @Value("${app.max.size}")
    private String maxUploadSize;


    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipient(Model model, @RequestParam("recipients") String recipients, RedirectAttributes redirectAttributes) {
        logger.debug("User added recipient.");
//        fileDropService.addRecipient(userContextService.getCurrentUser(), recipient);
//        LdapPerson ldapPerson = ldapService.findByUid(recipient);
        String[] recipientsArray = recipients.split(",");
        redirectAttributes.addFlashAttribute("recipients", recipientsArray);
        return "redirect:/prepare/files";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files")
    public String addFiles(Model model) {
        logger.debug("User at addFiles.");
//        logger.debug(recipient);
        model.addAttribute("maxUploadSize", maxUploadSize);
        return "user/files";
    }
}
