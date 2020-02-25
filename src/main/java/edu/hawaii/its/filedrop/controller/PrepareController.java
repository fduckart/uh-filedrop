package edu.hawaii.its.filedrop.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.service.ProcessVariableHolder;
import edu.hawaii.its.filedrop.service.WorkflowService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
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

    @Autowired
    private EmailService emailService;

    @Value("${app.max.size}")
    private String maxUploadSize;

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = { "/prepare" })
    public String prepare(Model model) {
        logger.debug("User at prepare.");

        Task currentTask = workflowService.getCurrentTask(currentUser());

        if (currentTask != null && currentTask.getTaskDefinitionKey().equalsIgnoreCase("filesTask")) {
            FileDrop fileDrop =
                    fileDropService.findFileDrop(fileDropService.getFileDropId(currentUser()));
            workflowService.revertTask(currentUser(), "recipientsTask");
            ProcessVariableHolder processVariableHolder =
                    new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
            String recipients = Arrays.toString(fileDrop.getRecipients().toArray());
            model.addAttribute("expiration", processVariableHolder.get("expirationLength"));
            model.addAttribute("authentication", fileDrop.isAuthenticationRequired());
            model.addAttribute("recipients", recipients);
            model.addAttribute("message", processVariableHolder.get("message"));
        } else {
            fileDropService.startUploadProcess(currentUser());
        }

        model.addAttribute("user", ldapService.findByUhUuidOrUidOrMail(currentUser().getUhuuid()));

        if (logger.isDebugEnabled()) {
            logger.debug("User: " + currentUser());
            logger.debug("Current Task: " + currentTask);
        }

        return "user/prepare";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipients(@RequestParam("sender") String sender,
            @RequestParam("validation") Boolean validation,
            @RequestParam("expiration") Integer expiration,
            @RequestParam("recipients") String[] recipients,
            @RequestParam("message") String message) {

        User user = currentUser();

        if (recipients.length == 0) {
            recipients = new String[1];
            recipients[0] = currentUser().getAttribute("uhEmail");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("User: " + currentUser());
            logger.debug("User added recipients: " + Arrays.toString(recipients));
        }

        FileDrop fileDrop;

        if (workflowService.hasFileDrop(user)) {
            fileDrop = fileDropService.findFileDrop(fileDropService.getFileDropId(user));
            fileDrop.setValid(validation);
            fileDrop.setAuthenticationRequired(validation);
            fileDrop.setRecipient(Arrays.toString(recipients));
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
        processVariableHolder.add("sender", sender);
        processVariableHolder.add("message", message);

        workflowService.addProcessVariables(workflowService.getCurrentTask(user), processVariableHolder.getMap());

        fileDropService.addRecipients(user, recipients);

        logger.debug(user.getUsername() + " created new " + fileDrop);
        logger.debug("Sender: " + sender);

        return "redirect:/prepare/files/" + fileDrop.getUploadKey();
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files/{uploadKey}")
    public String addFiles(Model model, @PathVariable String uploadKey) {

        if (workflowService.atTask(currentUser(), "addRecipients")) {
            return "redirect:/prepare";
        }

        logger.debug("User at addFiles.");

        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        List<String> recipientsList = fileDrop.getRecipients().stream().map(recipient -> {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);
            if (ldapPerson.isValid()) {
                return ldapPerson.getCn();
            }
            return recipient;
        }).collect(toList());

        model.addAttribute("recipients", recipientsList);
        model.addAttribute("maxUploadSize", maxUploadSize);
        model.addAttribute("uploadKey", fileDrop.getUploadKey());
        return "user/files";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare/files/{uploadKey}")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFiles(@RequestParam MultipartFile file, @RequestParam String comment,
            @PathVariable String uploadKey) {
        Task currentTask = workflowService.getCurrentTask(currentUser());
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        ProcessVariableHolder processVariables =
                new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
        Integer expiration = (Integer) processVariables.get("expirationLength");

        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(fileDrop.getCreated().plus(expiration, ChronoUnit.MINUTES));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileDropService.uploadFile(currentUser(), file, comment, fileDrop);
    }

    @GetMapping(value = "/complete/{uploadKey}")
    public String completeFileDrop(@PathVariable String uploadKey) {
        Task currentTask = workflowService.getCurrentTask(currentUser());
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        boolean isUploader = fileDrop.getUploader().equals(currentUser().getUsername());

        if (!isUploader) {
            return "redirect:/dl/" + fileDrop.getDownloadKey();
        }

        ProcessVariableHolder processVariables =
                new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));

        String sender = (String) processVariables.get("sender");
        Mail mail = new Mail();
        mail.setTo(sender);
        mail.setFrom(emailService.getFrom());

        Map<String, Object> fileDropContext = emailService.getFileDropContext("uploader", fileDrop);
        emailService.send(mail, "uploader", new Context(Locale.ENGLISH, fileDropContext));

        mail.setFrom(sender);

        long size = 0;

        for(FileSet fileSet : fileDrop.getFileSet()) {
            size += fileSet.getSize();
        }

        for (String recipient : fileDrop.getRecipients()) {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);

            if (ldapPerson.isValid()) {
                mail.setTo(ldapPerson.getMails().get(0));
            } else {
                mail.setTo(recipient);
            }

            fileDropContext = emailService.getFileDropContext("receiver", fileDrop);
            fileDropContext.put("comment", processVariables.get("message"));
            fileDropContext.put("size", size);
            fileDropContext.put("sender", sender);
            emailService.send(mail, "receiver", new Context(Locale.ENGLISH, fileDropContext));
        }
        fileDropService.completeFileDrop(currentUser(), fileDrop);

        return "redirect:/dl/" + fileDrop.getDownloadKey();
    }

    @GetMapping(value = "/helpdesk")
    public String prepareHelpdesk() {
        logger.debug("User at prepare-helpdesk");
        return "user/prepare-helpdesk";
    }

    @PostMapping(value = "/helpdesk")
    public String addHelpdesk(@RequestParam("sender") String sender, @RequestParam("expiration") Integer expiration,
            RedirectAttributes redirectAttributes) {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setUploader(sender);
        fileDrop.setUploaderFullName(sender);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setRecipient("[help]");
        fileDrop.setEncryptionKey(Strings.generateRandomString());
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop.setUploadKey(Strings.generateRandomString());
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(fileDrop.getCreated().plus(expiration, ChronoUnit.MINUTES));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        logger.debug("Sender: " + sender);
        logger.debug("Expiration: " + expiration);
        logger.debug("Download Key: " + fileDrop.getDownloadKey());

        redirectAttributes.addAttribute("downloadKey", fileDrop.getDownloadKey())
                .addFlashAttribute("expiration", expiration);
        return "redirect:/helpdesk/files/{downloadKey}";
    }

    @GetMapping(value = "/helpdesk/files/{downloadKey}")
    public String addFileHelpDesk(Model model, @PathVariable("downloadKey") String downloadKey) {

        List<String> recipients = Collections.singletonList("ITS Help Desk");
        model.addAttribute("maxUploadSize", maxUploadSize);
        model.addAttribute("downloadKey", downloadKey);
        model.addAttribute("recipients", recipients);

        return "user/files-helpdesk";
    }

    @PostMapping(value = "/helpdesk/files/{downloadKey}")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFilesHelpdesk(@PathVariable("downloadKey") String downloadKey, @RequestParam MultipartFile file,
            @RequestParam("comment") String comment, @RequestParam("expiration") String expirationStr) {
        FileSet fileSet = new FileSet();
        fileSet.setFileName(file.getOriginalFilename());
        fileSet.setType(file.getContentType());
        fileSet.setComment(comment);

        Integer expiration = Integer.valueOf(expirationStr);
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(fileDrop.getCreated().plus(expiration, ChronoUnit.MINUTES));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileSet.setFileDrop(fileDrop);
        fileDropService.saveFileSet(fileSet);

        logger.debug(fileDrop.getUploader() + " uploaded: " + fileSet);
    }

    @GetMapping(value = "/helpdesk/successful")
    public String helpdeskSuccessful(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("uploaded", true);
        return "redirect:/";
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }
}
