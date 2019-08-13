package edu.hawaii.its.filedrop.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        args.put("initiator", user.getUsername());
        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        runtimeService.setVariables(process.getId(), args);
        logger.debug("Created tasks for: " + user.getUsername());
    }

    public void addRecipient(User user, String[] recipients) {
        if (getCurrentTask(user).getName().equalsIgnoreCase("addRecipients")) {
            Task recipientTask = taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
            if (recipients.length == 0) {
                recipients = new String[1];
                recipients[0] = user.getUsername();
            }
            logger.debug(user.getUsername() + " added recipients: " + Arrays.toString(recipients));
            runtimeService.setVariable(recipientTask.getProcessInstanceId(), "recipients", recipients);
            taskService.complete(recipientTask.getId());
        }
    }

    public Task getCurrentTask(User user) {
        return taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
    }

    public void addProcessVariables(String processId, Map<String, Object> variables) {
        runtimeService.setVariables(processId, variables);
    }

    public void addTaskVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
    }

    public Map<String, Object> getProcessVariables(String processId) {
        return runtimeService.getVariables(processId);
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    public void uploadFile(User user, List<MultipartFile> files) {
        if (getCurrentTask(user).getName().equalsIgnoreCase("addFiles")) {
            logger.debug(user.getUsername() + " uploaded files.");
            Task filesTask = taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
            runtimeService.setVariable(filesTask.getProcessInstanceId(), "files", files);
            taskService.complete(filesTask.getId());
        }
    }
}
