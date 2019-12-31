package edu.hawaii.its.filedrop.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

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
import edu.hawaii.its.filedrop.service.ProcessVariableHolder;
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
    @GetMapping(value = { "/prepare" })
    public String prepare(Model model, @RequestParam(value = "helpdesk", required = false) Boolean helpdesk) {
        logger.debug("User at prepare.");

        Task currentTask = workflowService.getCurrentTask(currentUser());

        if (currentTask != null && currentTask.getTaskDefinitionKey().equalsIgnoreCase("filesTask")) {
            FileDrop fileDrop =
                    fileDropService.findFileDrop(fileDropService.getFileDropId(currentUser()));
            workflowService.revertTask(currentUser(), "recipientsTask");
            ProcessVariableHolder processVariableHolder =
                    new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
            String recipients = Arrays.toString((String[]) processVariableHolder.get("recipients"));
            model.addAttribute("expiration", processVariableHolder.get("expirationLength"));
            model.addAttribute("authentication", fileDrop.isAuthenticationRequired());
            model.addAttribute("recipients", recipients);
        } else {
            fileDropService.startUploadProcess(currentUser());
        }

        model.addAttribute("user", currentUser().getUsername() + "@hawaii.edu");
        model.addAttribute("helpdesk", helpdesk);

        if (logger.isDebugEnabled()) {
            logger.debug("User: " + currentUser());
            logger.debug("Helpdesk: " + helpdesk);
            logger.debug("Current Task: " + currentTask);
        }

        return "user/prepare";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipients(@RequestParam("sender") String sender,
            @RequestParam("validation") Boolean validation,
            @RequestParam("expiration") Integer expiration,
            @RequestParam("recipients") String[] recipients) {

        User user = currentUser();

        if (recipients.length == 0) {
            recipients = new String[1];
            recipients[0] = currentUser().getUsername();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("User: " + currentUser());
            logger.debug("User added recipients: " + Arrays.toString(recipients));
        }

        FileDrop fileDrop;

        if (workflowService.hasFileDrop(user)) {
            fileDrop = fileDropService.findFileDrop(fileDropService.getFileDropId(user));
            fileDrop.setRecipient(Arrays.toString(recipients));
            fileDrop.setValid(validation);
            fileDrop.setAuthenticationRequired(validation);
        } else {
            fileDrop = new FileDrop();
            fileDrop.setRecipient(Arrays.toString(recipients));
            fileDrop.setEncryptionKey(Strings.generateRandomString());
            fileDrop.setDownloadKey(Strings.generateRandomString());
            fileDrop.setUploadKey(Strings.generateRandomString());
            fileDrop.setUploader(user.getUsername());
            fileDrop.setUploaderFullName(user.getName());
            fileDrop.setValid(validation);
            fileDrop.setAuthenticationRequired(validation);
        }

        fileDrop = fileDropService.saveFileDrop(fileDrop);

        ProcessVariableHolder processVariableHolder = new ProcessVariableHolder();
        processVariableHolder.add("fileDropId", fileDrop.getId());
        processVariableHolder.add("fileDropDownloadKey", fileDrop.getDownloadKey());
        processVariableHolder.add("expirationLength", expiration);

        workflowService.addProcessVariables(workflowService.getCurrentTask(user), processVariableHolder.getMap());

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

        ProcessVariableHolder processVariables =
                new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
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
        model.addAttribute("downloadKey", processVariables.get("fileDropDownloadKey"));
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
        ProcessVariableHolder processVariables =
                new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
        Integer fileDropId = (Integer) processVariables.get("fileDropId");
        FileDrop fileDrop = fileDropService.findFileDrop(fileDropId);
        Integer expiration = (Integer) processVariables.get("expirationLength");
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(fileDrop.getCreated().plus(expiration, ChronoUnit.MINUTES));
        fileDropService.saveFileDrop(fileDrop);
        fileSet.setFileDrop(fileDrop);
        fileDropService.saveFileSet(fileSet);

        logger.debug(currentUser().getUsername() + " uploaded: " + fileSet);
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }
}
