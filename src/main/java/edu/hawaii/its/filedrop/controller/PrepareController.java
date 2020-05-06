package edu.hawaii.its.filedrop.controller;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import edu.hawaii.its.filedrop.service.WhitelistService;
import edu.hawaii.its.filedrop.service.WorkflowService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Recipient;
import edu.hawaii.its.filedrop.util.Dates;
import edu.hawaii.its.filedrop.util.Strings;

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

    @Autowired
    private WhitelistService whitelistService;

    @Value("${app.mail.help}")
    private String helpName;

    @Value("${app.mail.to.help}")
    private String helpEmail;

    @Value("${app.max.size}")
    private String maxUploadSize;

    @GetMapping(value = "/helpdesk/files/{uploadKey}")
    public String addFileHelpDesk(Model model, @PathVariable String uploadKey) {
        model.addAttribute("maxUploadSize", maxUploadSize);
        model.addAttribute("uploadKey", uploadKey);
        model.addAttribute("recipients", Arrays.asList(helpName));

        return "user/files-helpdesk";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files/{uploadKey}")
    public String addFiles(Model model, @PathVariable String uploadKey) {

        logger.debug("User at addFiles.");
        User user = currentUser();

        if (workflowService.atTask(user, "addRecipients")) {
            return "redirect:/prepare";
        }

        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);

        ProcessVariableHolder processVariableHolder =
                new ProcessVariableHolder(workflowService.getProcessVariables(user));

        String[] recipients = processVariableHolder.getStrings("recipients");

        List<String> recipientsList = Arrays.stream(recipients).map(recipient -> {
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

    @PostMapping(value = "/helpdesk")
    public String addHelpdesk(@RequestParam String sender,
            @RequestParam Integer expiration,
            @RequestParam Integer ticketNumber,
            RedirectAttributes redirectAttributes) {

        FileDrop fileDrop = new FileDrop();
        fileDrop.setUploader(sender);
        fileDrop.setUploaderFullName(sender);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setEncryptionKey(Strings.generateRandomString());
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop.setUploadKey(Strings.generateRandomString());

        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileDropService.addRecipients(fileDrop, helpEmail);

        logger.debug("Sender: " + sender);
        logger.debug("Recipient: " + fileDrop.getRecipients());
        logger.debug("Expiration: " + expiration);
        logger.debug("Ticket Number: " + ticketNumber);

        logger.debug("Upload Key: " + fileDrop.getUploadKey());

        redirectAttributes.addAttribute("uploadKey", fileDrop.getUploadKey())
                .addFlashAttribute("expiration", expiration)
                .addFlashAttribute("ticketNumber", ticketNumber);

        return "redirect:/helpdesk/files/{uploadKey}";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipients(@RequestParam("sender") String sender,
            @RequestParam("validation") Boolean validation,
            @RequestParam("expiration") Integer expiration,
            @RequestParam("recipients") String[] recipients,
            @RequestParam("message") String message) {

        User user = currentUser();

        if (logger.isDebugEnabled()) {
            logger.debug("User: " + user);
            logger.debug("User added recipients: " + Arrays.toString(recipients));
        }

        FileDrop fileDrop;

        if (workflowService.hasFileDrop(user)) {
            fileDrop = fileDropService.findFileDrop(fileDropService.getFileDropId(user));
            fileDrop.setAuthenticationRequired(validation);
        } else {
            fileDrop = new FileDrop();
            fileDrop.setEncryptionKey(Strings.generateRandomString());
            fileDrop.setDownloadKey(Strings.generateRandomString());
            fileDrop.setUploadKey(Strings.generateRandomString());
            fileDrop.setUploader(user.getUsername());
            fileDrop.setUploaderFullName(user.getName());
            fileDrop.setAuthenticationRequired(validation);
        }

        fileDrop.setValid(true);
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        ProcessVariableHolder processVariableHolder = new ProcessVariableHolder();
        processVariableHolder.add("fileDropId", fileDrop.getId());
        processVariableHolder.add("fileDropDownloadKey", fileDrop.getDownloadKey());
        processVariableHolder.add("expirationLength", expiration);
        processVariableHolder.add("sender", sender);
        processVariableHolder.add("message", message);

        workflowService.addProcessVariables(user, processVariableHolder);

        fileDropService.addRecipients(user, recipients);

        for (String recipient : recipients) {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);
            boolean checkRecipient = fileDropService.checkRecipient(user, ldapPerson, fileDrop.isAuthenticationRequired());
            logger.debug("checkRecipient; " + recipient + ": " + checkRecipient);

            if (!checkRecipient) {
                return "redirect:/prepare";
            }
        }

        logger.debug(user.getUsername() + " created new " + fileDrop);
        logger.debug("Sender: " + sender);

        return "redirect:/prepare/files/" + fileDrop.getUploadKey();
    }

    @GetMapping(value = "/complete/{uploadKey}")
    public String completeFileDrop(@PathVariable String uploadKey) {
        logger.debug("completeFileDrop; start.");
        logger.info("completeFileDrop; uploadKey: " + uploadKey);

        Task currentTask = workflowService.getCurrentTask(currentUser());
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);

        boolean isUploader = fileDrop.getUploader().equals(currentUser().getUsername());
        logger.debug("completeFileDrop; isUploader: " + isUploader);
        if (!isUploader) {
            return "redirect:/dl/" + fileDrop.getDownloadKey();
        }

        ProcessVariableHolder processVariables =
                new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));

        Integer expiration = processVariables.getInteger("expirationLength");
        String[] recipients = processVariables.getStrings("recipients");

        LocalDateTime now = LocalDateTime.now();
        fileDrop.setCreated(now);
        fileDrop.setExpiration(Dates.addMinutes(now, expiration));
        fileDrop.setValid(true);
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileDropService.addRecipients(fileDrop, recipients);

        String sender = processVariables.getString("sender");
        Mail mail = new Mail();
        mail.setTo(sender);
        mail.setFrom(emailService.getFrom());

        Map<String, Object> fileDropContext = emailService.getFileDropContext("uploader", fileDrop);
        emailService.send(mail, "uploader", new Context(Locale.ENGLISH, fileDropContext));

        mail.setFrom(sender);

        long size = totalFilesize(fileDrop);

        for (Recipient recipient : fileDropService.findRecipients(fileDrop)) {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient.getName());

            if (ldapPerson.isValid()) {
                mail.setTo(ldapPerson.getMails().get(0));
            } else {
                mail.setTo(recipient.getName());
            }

            fileDropContext = emailService.getFileDropContext("receiver", fileDrop);
            fileDropContext.put("comment", processVariables.getString("message"));
            fileDropContext.put("size", size);
            fileDropContext.put("sender", sender);
            emailService.send(mail, "receiver", new Context(Locale.ENGLISH, fileDropContext));
        }
        fileDropService.completeFileDrop(currentUser(), fileDrop);

        logger.debug("completeFileDrop; done.");

        return "redirect:/dl/" + fileDrop.getDownloadKey();
    }

    private long totalFilesize(FileDrop fileDrop) {
        long size = 0;
        for (FileSet fileSet : fileDrop.getFileSet()) {
            size += fileSet.getSize();
        }
        return size;
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }

    @GetMapping(value = "/helpdesk/successful/{uploadKey}")
    public String helpdeskSuccessful(RedirectAttributes redirectAttributes, @PathVariable String uploadKey,
            @RequestParam String expiration, @RequestParam String ticketNumber) {
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        LocalDateTime now = LocalDateTime.now();
        fileDrop.setCreated(now);
        fileDrop.setExpiration(Dates.addMinutes(now, Integer.parseInt(expiration)));
        fileDrop.setValid(true);

        Mail mail = new Mail();
        mail.setTo(helpEmail);
        mail.setFrom(fileDrop.getUploader());
        mail.setSubject("FileDrop Helpdesk Ticket: " + ticketNumber);

        redirectAttributes.addFlashAttribute("message", "File(s) uploaded <strong>successfully</strong>");
        return "redirect:/";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = { "/prepare" })
    public String prepare(Model model) {
        logger.debug("User at prepare.");

        User user = currentUser();
        Task currentTask = workflowService.getCurrentTask(user);

        if (currentTask != null && currentTask.getTaskDefinitionKey().equalsIgnoreCase("filesTask")) {
            FileDrop fileDrop =
                    fileDropService.findFileDrop(fileDropService.getFileDropId(user));

            workflowService.revertTask(user, "recipientsTask");
            ProcessVariableHolder processVariableHolder =
                    new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
            String recipients = Arrays.toString(processVariableHolder.getStrings("recipients"));
            model.addAttribute("sender", processVariableHolder.getString("sender"));
            model.addAttribute("expiration", processVariableHolder.getInteger("expirationLength"));
            model.addAttribute("authentication", fileDrop.isAuthenticationRequired());
            model.addAttribute("recipients", recipients);
            model.addAttribute("message", processVariableHolder.getString("message"));
        } else {
            fileDropService.startUploadProcess(user);
        }

        model.addAttribute("uid", user.getUid());
        model.addAttribute("mails", user.getAttributes().getMail());
        model.addAttribute("affiliations", user.getAttributes().getAffiliation());
        model.addAttribute("whitelist", whitelistService.getAllWhitelistUids());

        if (logger.isDebugEnabled()) {
            logger.debug("User: " + user);
            logger.debug("Current Task: " + currentTask);
        }

        return "user/prepare";
    }

    @GetMapping(value = "/helpdesk")
    public String prepareHelpdesk(Model model) {
        logger.debug("User at prepare-helpdesk");
        model.addAttribute("recipient", helpName);
        model.addAttribute("recipientEmail", helpEmail);
        return "user/prepare-helpdesk";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare/files/{uploadKey}")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFiles(@RequestParam MultipartFile file, @RequestParam String comment,
            @PathVariable String uploadKey) {
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        fileDropService.uploadFile(currentUser(), file, comment, fileDrop);
    }

    @PostMapping(value = "/helpdesk/files/{uploadKey}")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFilesHelpdesk(@PathVariable String uploadKey, @RequestParam MultipartFile file,
            @RequestParam("comment") String comment) {
        FileSet fileSet = new FileSet();
        fileSet.setFileName(file.getOriginalFilename());
        fileSet.setType(file.getContentType());
        fileSet.setComment(comment);
        fileSet.setSize(file.getSize());

        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);

        fileSet.setFileDrop(fileDrop);
        fileDropService.saveFileSet(fileSet);

        if (logger.isDebugEnabled()) {
            logger.debug("uploadFilesHelpdesk; uploadKey: " + uploadKey);
            logger.debug("uploadFilesHelpdesk;    uploader: " + fileDrop.getUploader());
            logger.debug("uploadFilesHelpdesk;     fileSet: " + fileSet);
            logger.debug("uploadFilesHelpdesk;    fileDrop: " + fileDrop);
        }
    }
}
