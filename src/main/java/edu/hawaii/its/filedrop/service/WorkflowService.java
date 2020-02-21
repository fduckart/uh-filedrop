package edu.hawaii.its.filedrop.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.access.User;

@Service
public class WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    public void startProcess(User user, String processKey, Map<String, Object> args) {
        if (hasTask(user)) {
            List<Task> tasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();
            for (Task task : tasks) {
                runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "restart");
            }
        }
        runtimeService.startProcessInstanceByKey(processKey, args);
    }

    public void startProcess(User user, String processKey) {
        Map<String, Object> args = Collections.singletonMap("initiator", user.getUsername());
        startProcess(user, processKey, args);
    }

    public void stopProcess(User user) {
        Task task = getCurrentTask(user);
        if (task != null) {
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "stop");
        }
    }

    public void revertTask(User user, String previousTask) {
        Task task = getCurrentTask(user);
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(), previousTask)
                .changeState();
    }

    public boolean atTask(User user, String taskName) {
        return hasTask(user) && getCurrentTask(user).getName().equalsIgnoreCase(taskName);
    }

    public boolean hasTask(User user) {
        return getCurrentTask(user) != null;
    }

    public Task getCurrentTask(User user) {
        return taskService.createTaskQuery().taskAssignee(user.getUsername()).singleResult();
    }

    public void completeCurrentTask(User user) {
        taskService.complete(getCurrentTask(user).getId());
    }

    public void addProcessVariables(String processId, Map<String, Object> variables) {
        runtimeService.setVariables(processId, variables);
    }

    public void addProcessVariables(Task task, Map<String, Object> variables) {
        addProcessVariables(task.getProcessInstanceId(), variables);
    }

    public Map<String, Object> getProcessVariables(String processId) {
        return runtimeService.getVariables(processId);
    }

    public Map<String, Object> getProcessVariables(Task task) {
        return getProcessVariables(task.getProcessInstanceId());
    }

    public Map<String, Object> getProcessVariables(User user) {
        return getProcessVariables(getCurrentTask(user));
    }

    public boolean hasFileDrop(User user) {
        return getProcessVariables(getCurrentTask(user)).containsKey("fileDropId");
    }
}
