package edu.hawaii.its.filedrop.service;

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

@Service
public class FileDropService {

    private static final Log logger = LogFactory.getLog(FileDropService.class);

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    public void startUploadProcess(User user) {
        if (workflowService.hasTask(user)) {
            Integer fileDropId =
                    (Integer) workflowService
                            .getProcessVariables(workflowService.getCurrentTask(user).getProcessInstanceId())
                            .get("fileDropId");
            if (getFileDrop(fileDropId) != null) {
                fileDropRepository.delete(getFileDrop(fileDropId));
            }
        }
        workflowService.startProcess(user, "fileUpload");
        logger.debug("Created tasks for: " + user.getUsername());
    }

    public void addRecipients(User user, String... recipients) {
        if (workflowService.atTask(user, "addRecipients")) {
            Task recipientTask = workflowService.getCurrentTask(user);
            if (recipients.length == 0) {
                recipients = new String[1];
                recipients[0] = user.getUsername();
            }
            logger.debug(user.getUsername() + " added recipients: " + Arrays.toString(recipients));
            workflowService.addProcessVariables(recipientTask.getProcessInstanceId(),
                    Collections.singletonMap("recipients", recipients));
            workflowService.completeCurrentTask(user);
        }
    }

    public void uploadFile(User user, List<MultipartFile> files) {
        if (workflowService.atTask(user, "addFiles")) {
            logger.debug(user.getUsername() + " uploaded files.");
            Task filesTask = workflowService.getCurrentTask(user);
            workflowService
                    .addProcessVariables(filesTask.getProcessInstanceId(), Collections.singletonMap("files", files));
            workflowService.completeCurrentTask(user);
        }
    }

    public FileSet saveFileSet(FileSet fileSet) {
        return fileSetRepository.save(fileSet);
    }

    public List<FileSet> getFileSets(FileDrop fileDrop) {
        return fileSetRepository.findAllByFileDrop(fileDrop);
    }

    public FileDrop saveFileDrop(FileDrop fileDrop) {
        return fileDropRepository.save(fileDrop);
    }

    public FileDrop getFileDrop(Integer id) {
        return fileDropRepository.findOne(withId(id)).orElse(null);
    }

    public FileDrop getFileDrop(String key) {
        return fileDropRepository.findOne(withDownloadKey(key)).orElse(null);
    }
}
