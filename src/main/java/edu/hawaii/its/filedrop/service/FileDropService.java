package edu.hawaii.its.filedrop.service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.repository.FileSetRepository;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withDownloadKey;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withId;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withUploadKey;

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
    private FileSystemStorageService fileSystemStorageService;

    public void startUploadProcess(User user) {
        if (workflowService.hasTask(user)) {
            Integer fileDropId = getFileDropId(user);
            if (fileDropId != null && findFileDrop(fileDropId) != null) {
                fileDropRepository.delete(findFileDrop(fileDropId));
            }
        }
        workflowService.startProcess(user, "fileUpload");
        logger.debug("Created tasks for: " + user.getUsername());
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

            fileSystemStorageService.storeFileSet(file, Paths.get(fileDrop.getDownloadKey(),
                    fileSet.getId().toString()));

            logger.debug(user.getUsername() + " uploaded " + fileSet);
        }
    }

    public void completeFileDrop(User user, FileDrop fileDrop) {
        if (workflowService.atTask(user, "addFiles")) {
            logger.debug(user.getUsername() + " completed " + fileDrop);
            workflowService.completeCurrentTask(user);
        }
    }

    public boolean isAuthorized(FileDrop fileDrop, String user) {
        return fileDrop.getRecipients().contains(user) || fileDrop.getUploader().equals(user);
    }

    public FileSet saveFileSet(FileSet fileSet) {
        return fileSetRepository.save(fileSet);
    }

    public List<FileSet> findFileSets(FileDrop fileDrop) {
        return fileSetRepository.findAllByFileDrop(fileDrop);
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
}
