package edu.hawaii.its.filedrop.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapService;

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
    public String addRecipients(@RequestParam("recipients") String[] recipients) {
        logger.debug("User added recipient.");
//        LdapPerson ldapPerson = ldapService.findByUid(recipient);
        fileDropService.addRecipient(userContextService.getCurrentUser(), recipients);
        return "redirect:/prepare/files";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files")
    public String addFiles(Model model) {
        Task currentTask = fileDropService.getCurrentTask(userContextService.getCurrentUser());
        if (currentTask == null || currentTask.getName().equalsIgnoreCase("recipientsTask")) {
            return "redirect:/prepare";
        }
        logger.debug("User at addFiles.");
        model.addAttribute("recipients", fileDropService.getProcessVariables(currentTask.getProcessInstanceId()).get("recipients"));
        model.addAttribute("maxUploadSize", maxUploadSize);
        return "user/files";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare/files")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files) {
        logger.debug(userContextService.getCurrentUser().getUsername() + " uploaded: " + Arrays.toString(files));
        return "redirect:/home";
    }
}
