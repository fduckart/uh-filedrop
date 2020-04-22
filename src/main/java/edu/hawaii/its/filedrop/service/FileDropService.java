package edu.hawaii.its.filedrop.service;

import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withDownloadKey;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withId;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withUploadKey;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.repository.FileSetRepository;
import edu.hawaii.its.filedrop.repository.RecipientRepository;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Recipient;

@Service
public class FileDropService {

    private static final Log logger = LogFactory.getLog(FileDropService.class);

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @Autowired
    private WhitelistService whitelistService;

    @Value("${app.restrictions.sender.student}")
    private List<String> studentRestrictions;

    @Value("${app.restrictions.sender.faculty}")
    private List<String> facultyRestrictions;

    @Value("${app.restrictions.sender.staff}")
    private List<String> staffRestrictions;

    @Value("${app.restrictions.sender.affiliate}")
    private List<String> affiliateRestrictions;

    @Value("${app.restrictions.sender.other}")
    private List<String> otherRestrictions;

    private Map<String, List<String>> allRestrictions;

    @PostConstruct
    public void init() {
        allRestrictions = new HashMap<>();
        allRestrictions.put("affiliate", affiliateRestrictions);
        allRestrictions.put("faculty", facultyRestrictions);
        allRestrictions.put("other", otherRestrictions);
        allRestrictions.put("staff", staffRestrictions);
        allRestrictions.put("student", studentRestrictions);
    }

    public void expire(FileDrop fileDrop) {
        fileDrop.setValid(false);
        saveFileDrop(fileDrop);
    }

    public void startUploadProcess(User user) {
        deleteUploadProcess(user);
        workflowService.startProcess(user, "fileUpload");
        logger.debug("startUploadProcess; user: " + user);
    }

    protected void deleteUploadProcess(User user) {
        if (workflowService.hasTask(user)) {
            Integer fileDropId = fileDropId(user);
            if (fileDropId != null) {
                FileDrop fileDrop = findFileDrop(fileDropId);
                if (fileDrop != null) {
                    fileDropRepository.delete(fileDrop);
                }
            }
        }
    }

    public Integer fileDropId(User user) {
        return getFileDropId(user);
    }

    public Integer getFileDropId(User user) {
        return (Integer) workflowService.getProcessVariables(user).get("fileDropId");
    }

    public void addRecipients(User user, String... recipients) {
        if (workflowService.atTask(user, "addRecipients")) {
            Task recipientTask = workflowService.getCurrentTask(user);
            logger.debug(user.getUsername() + " added recipients: " + Arrays.toString(recipients));
            workflowService.addProcessVariables(recipientTask, Collections.singletonMap("recipients", recipients));
            workflowService.completeCurrentTask(user);
        }
    }

    public void uploadFile(User user, MultipartFile file, String comment, FileDrop fileDrop) {
        if (workflowService.atTask(user, "addFiles") && file != null) {
            FileSet fileSet = new FileSet();
            fileSet.setFileName(file.getOriginalFilename());
            fileSet.setType(file.getContentType());
            fileSet.setComment(comment);
            fileSet.setFileDrop(fileDrop);
            fileSet.setSize(file.getSize());
            saveFileSet(fileSet);

            fileSystemStorageService.storeFileSet(file,
                    Paths.get(fileDrop.getDownloadKey(), fileSet.getId().toString()));

            logger.debug(user.getUsername() + " uploaded " + fileSet);
        }
    }

    public void completeFileDrop(User user, FileDrop fileDrop) {
        if (workflowService.atTask(user, "addFiles")) {
            logger.debug(user.getUsername() + " completed " + fileDrop);
            workflowService.completeCurrentTask(user);
        }
    }

    public void addRecipients(FileDrop fileDrop, String... recipients) {
        List<Recipient> recipientList = new ArrayList<>();

        Arrays.stream(recipients).forEach(recipient -> {
            Recipient recipientObj = new Recipient();
            recipientObj.setName(recipient);
            recipientObj.setFileDrop(fileDrop);

            if (!containsRecipient(fileDrop, recipient)) {
                recipientList.add(recipientObj);
            }
        });

        recipientRepository.saveAll(recipientList);
        saveFileDrop(fileDrop);
    }

    public boolean isAuthorized(FileDrop fileDrop, String user) {
        return containsRecipient(fileDrop, user) || fileDrop.getUploader().equals(user);
    }

    public boolean containsRecipient(FileDrop fileDrop, String recipient) {
        return findRecipients(fileDrop)
                .stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(recipient));
    }

    public boolean checkRecipient(User user, LdapPerson ldapPerson, boolean authRequired) {
        if (user.getUid().equals(ldapPerson.getUid())) {
            return true;
        }

        if (!authRequired && !ldapPerson.isValid()) {
            return true;
        }

        boolean validRecipient = false;

        for (String affiliations : user.getAttributes().getAffiliation()) {
            validRecipient = ldapPerson.getAffiliations().stream()
                    .anyMatch(affiliation -> allRestrictions.get(affiliations).contains(affiliation));

            if (allRestrictions.get(affiliations).contains("department") && !validRecipient) {
                validRecipient = whitelistService.isWhitelisted(ldapPerson.getUid());
            }

            if (validRecipient) {
                break;
            }
        }

        return validRecipient;
    }

    public FileSet saveFileSet(FileSet fileSet) {
        return fileSetRepository.save(fileSet);
    }

    public List<FileSet> findFileSets(FileDrop fileDrop) {
        return fileSetRepository.findAllByFileDrop(fileDrop);
    }

    public List<Recipient> findRecipients(FileDrop fileDrop) {
        return recipientRepository.findAllByFileDrop(fileDrop);
    }

    public FileDrop saveFileDrop(FileDrop fileDrop) {
        return fileDropRepository.save(fileDrop);
    }

    public FileDrop findFileDrop(Integer id) {
        return fileDropRepository.findOne(withId(id)).orElse(null);
    }

    public FileDrop findFileDropDownloadKey(String key) {
        return fileDropRepository.findOne(withDownloadKey(key)).orElse(null);
    }

    public FileDrop findFileDropUploadKey(String key) {
        return fileDropRepository.findOne(withUploadKey(key)).orElse(null);
    }

    public List<FileDrop> findAllFileDrop() {
        return fileDropRepository.findAll();
    }
}
