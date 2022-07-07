package edu.hawaii.its.filedrop.service;

import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.isExpiring;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.isValid;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withDownloadKey;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withId;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withRecipient;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withUploadKey;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withUploader;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specification.where;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.repository.DownloadRepository;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.repository.FileSetRepository;
import edu.hawaii.its.filedrop.repository.RecipientRepository;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileDropInfo;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Recipient;

@Service
public class FileDropService {

    private static final Log logger = LogFactory.getLog(FileDropService.class);

    @Autowired
    private AllowlistService allowlistService;

    @Autowired
    private CipherService cipherService;

    @Autowired
    private DownloadRepository downloadRepository;

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @Autowired
    private WorkflowService workflowService;

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
        Path directory = Paths.get(fileSystemStorageService.getRootLocation().toString(), fileDrop.getDownloadKey());
        fileDrop.setValid(false);
        List<FileSet> fileSets = fileSetRepository.findAllByFileDrop(fileDrop);
        fileSets.forEach(fileSet -> fileSystemStorageService.delete(fileSet.getId().toString(), directory.toString()));
        fileSystemStorageService.delete(directory);
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
            Task recipientTask = workflowService.currentTask(user);
            logger.debug(user.getUsername() + " added recipients: " + Arrays.toString(recipients));
            workflowService.addProcessVariables(recipientTask, Collections.singletonMap("recipients", recipients));
            workflowService.completeCurrentTask(user);
        }
    }

    public void uploadFile(User user, MultipartFile file, String comment, FileDrop fileDrop)
            throws IOException, GeneralSecurityException {
        if (workflowService.atTask(user, "addFiles") && file != null) {
            FileSet fileSet = new FileSet();
            fileSet.setFileName(file.getOriginalFilename());
            fileSet.setType(file.getContentType());
            fileSet.setComment(comment);
            fileSet.setFileDrop(fileDrop);
            fileSet.setSize(file.getSize());
            fileSet = saveFileSet(fileSet);

            Path path = Paths.get(fileSystemStorageService.getRootLocation().toString(),
                    fileSet.getFileDrop().getDownloadKey());
            path.toFile().mkdir();

            cipherService.encrypt(file.getInputStream(), fileSet);

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

        if (logger.isDebugEnabled()) {
            logger.debug("addRecipients; fileDrop: " + fileDrop);
            logger.debug("addRecipients; recipientList: " + recipientList);
        }

        ///recipientRepository.saveAll(recipientList);

        fileDrop.setRecipients(recipientList);

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
                validRecipient = allowlistService.isAllowlisted(ldapPerson.getUid());
            }

            if (validRecipient) {
                break;
            }
        }

        return validRecipient;
    }

    public synchronized void checkFileDrops() {
        logger.debug("Starting expired FileDrops check");

        List<FileDrop> expiredFileDrops = findAllExpiringFileDrops();

        expiredFileDrops.forEach(this::expire);

        logger.debug("Finished expired FileDrops check. " + expiredFileDrops.size() + " FileDrop(s) expired.");
    }

    public FileDropInfo getFileDropInfo(FileDrop fileDrop) {
        FileDropInfo fileDropInfo = new FileDropInfo();
        fileDropInfo.setUploader(fileDrop.getUploader());
        fileDropInfo.setCreated(fileDrop.getCreated());
        fileDropInfo.setExpiration(fileDrop.getExpiration());
        fileDropInfo.setFileDropId(fileDrop.getId());
        fileDropInfo.setValid(fileDrop.isValid());
        fileDropInfo.setDownloadKey(fileDrop.getDownloadKey());
        fileDropInfo.setRecipients(fileDrop.getRecipients().stream().map(Recipient::getName).collect(toList()));
        List<FileSet> fileSets = fileSetRepository.findAllByFileDrop(fileDrop);
        fileDropInfo.setFileInfoList(fileSets.stream().map(fileSet -> {
            FileDropInfo.FileInfo fileInfo = new FileDropInfo.FileInfo();
            fileInfo.setFileName(fileSet.getFileName());
            fileInfo.setFileSize(fileSet.getSize());
            fileInfo.setFileType(fileSet.getType());
            fileInfo.setDownloads(downloadRepository.findAllByFileDropAndFileName(fileDrop, fileSet.getFileName()).size());
            return fileInfo;
        }).collect(toList()));
        fileDropInfo.setDownloads(fileDropInfo.getFileInfoList().stream().mapToInt(FileDropInfo.FileInfo::getDownloads).sum());
        return fileDropInfo;
    }

    public List<FileDropInfo> convertToFileDropInfo(List<FileDrop> fileDrops) {
        return fileDrops.stream().map(this::getFileDropInfo).collect(toList());
    }

    public List<FileDropInfo> findAllUserFileDropInfo(User user) {
        return convertToFileDropInfo(findAllUserFileDrops(user));
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

    public List<FileDrop> findAllUserFileDrops(User user) {
        Set<FileDrop> fileDrops = new HashSet<>(fileDropRepository.findAll(
                where(withUploader(user.getUsername())
                        .or(withRecipient(user.getUsername())))));
        return new ArrayList<>(fileDrops);
    }

    public List<FileDrop> findAllValidFileDrops() {
        return fileDropRepository.findAll(isValid(true));
    }

    public List<FileDrop> findAllExpiringFileDrops() {
        return fileDropRepository.findAll(isExpiring());
    }

    public List<FileDrop> findAllFileDrops() {
        return fileDropRepository.findAll();
    }
}
