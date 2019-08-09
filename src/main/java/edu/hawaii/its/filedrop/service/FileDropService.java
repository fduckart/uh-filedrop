package edu.hawaii.its.filedrop.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.access.User;

@Service
public class FileDropService {

    private static final Log logger = LogFactory.getLog(FileDropService.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    public void startUploadProcess(User user) {
        if (getCurrentTask(user) != null) {
            runtimeService.deleteProcessInstance(getCurrentTask(user).getProcessInstanceId(), "restart");
        }
        logger.debug(user.getUsername() + " started upload process.");
        Map<String, Object> args = new HashMap<>();
        args.put("sender", user.getUsername());
        runtimeService.startProcessInstanceByKey("fileUpload", args);
        logger.debug("Created tasks for: " + user.getUsername());
    }

    public void addRecipient(User user, String recipient) {
        if (getCurrentTask(user).getName().equalsIgnoreCase("addRecipients")) {
            Task recipientTask = taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
            logger.debug(user.getUsername() + " added a recipient: " + recipient);
            Map<String, Object> args = new HashMap<>();
            args.put("recipient", recipient);
            taskService.complete(recipientTask.getId(), args);
        }
    }

    public Task getCurrentTask(User user) {
        return taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
    }

    public void uploadFile(User user) {
        if (getCurrentTask(user).getName().equalsIgnoreCase("addFiles")) {
            logger.debug(user.getUsername() + " uploaded files.");
            Task filesTask = taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
            taskService.complete(filesTask.getId());
        }
    }
}
