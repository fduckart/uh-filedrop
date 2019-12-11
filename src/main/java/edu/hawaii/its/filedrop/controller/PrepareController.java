package edu.hawaii.its.filedrop.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.service.WorkflowService;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.util.Strings;

import static java.util.stream.Collectors.toList;

@Controller
public class PrepareController {

    private Log logger = LogFactory.getLog(PrepareController.class);

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private LdapService ldapService;

    @Value("${app.max.size}")
    private String maxUploadSize;

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipients(@RequestParam("sender") String sender,
            @RequestParam("validation") Boolean validation,
            @RequestParam("expiration") Integer expiration,
            @RequestParam("recipients") String[] recipients) {

        User user = currentUser();

        if(recipients.length == 0) {
            recipients = new String[1];
            recipients[0] = currentUser().getUsername();
        }

        if(logger.isDebugEnabled()) {
            logger.debug("User: " + currentUser());
            logger.debug("User added recipients: " + Arrays.toString(recipients));
        }

        FileDrop fileDrop;

        if (workflowService.getProcessVariables(workflowService.getCurrentTask(user)).containsKey("fileDropId")) {
            fileDrop = fileDropService.findFileDrop(fileDropService.getFileDropId(user));
            fileDrop.setRecipient(Arrays.toString(recipients));
            fileDrop.setValid(validation);
            fileDrop.setAuthenticationRequired(validation);
            fileDrop.setExpiration(fileDrop.getCreated().plus(expiration, ChronoUnit.DAYS));
        } else {
            fileDrop = new FileDrop();
            fileDrop.setRecipient(Arrays.toString(recipients));
            fileDrop.setEncryptionKey(Strings.generateRandomString());
            fileDrop.setDownloadKey(Strings.generateRandomString());
            fileDrop.setUploadKey(Strings.generateRandomString());
            fileDrop.setUploader(user.getUsername());
            fileDrop.setUploaderFullName(user.getName());
            fileDrop.setCreated(LocalDateTime.now());
            fileDrop.setExpiration(fileDrop.getCreated().plus(expiration, ChronoUnit.DAYS));
            fileDrop.setValid(validation);
            fileDrop.setAuthenticationRequired(validation);
        }

        fileDrop = fileDropService.saveFileDrop(fileDrop);

        Map<String, Object> args = new HashMap<>();
        args.put("fileDropId", fileDrop.getId());
        args.put("fileDropDownloadKey", fileDrop.getDownloadKey());
        workflowService.addProcessVariables(workflowService.getCurrentTask(user), args);

        fileDropService.addRecipients(user, recipients);

        logger.debug(user.getUsername() + " created new " + fileDrop);
        logger.debug("Sender: " + sender);

        return "redirect:/prepare/files";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files")
    public String addFiles(Model model) {
        Task currentTask = workflowService.getCurrentTask(currentUser());

        if (workflowService.atTask(currentUser(), "addRecipients")) {
            return "redirect:/prepare";
        }

        logger.debug("User at addFiles.");

        Map<String, Object> processVariables = workflowService.getProcessVariables(currentTask);
        String[] recipients = (String[]) processVariables.get("recipients");
        List<String> recipientsList = Arrays.stream(recipients).map(recipient -> {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);
            if (ldapPerson.isValid()) {
                return ldapPerson.getCn();
            }
            return recipient;
        }).collect(toList());

        model.addAttribute("recipients", recipientsList);
        model.addAttribute("maxUploadSize", maxUploadSize);
        model.addAttribute("downloadKey", workflowService.getProcessVariables(currentTask).get("fileDropDownloadKey"));
        return "user/files";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare/files")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFiles(@RequestParam MultipartFile file, @RequestParam String comment) {
        FileSet fileSet = new FileSet();
        fileSet.setFileName(file.getOriginalFilename());
        fileSet.setType(file.getContentType());
        fileSet.setComment(comment);

        Task currentTask = workflowService.getCurrentTask(currentUser());
        Map<String, Object> processVariables = workflowService.getProcessVariables(currentTask);
        Integer fileDropId = (Integer) processVariables.get("fileDropId");
        fileSet.setFileDrop(fileDropService.findFileDrop(fileDropId));
        fileDropService.saveFileSet(fileSet);

        logger.debug(currentUser().getUsername() + " uploaded: " + fileSet);
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }
}
