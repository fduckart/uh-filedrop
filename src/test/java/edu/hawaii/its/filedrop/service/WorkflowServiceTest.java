package edu.hawaii.its.filedrop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.access.AnonymousUser;
import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.controller.WithMockUhUser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class WorkflowServiceTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private WorkflowService workflowService;

    @Test
    public void startProcessTest() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);
        assertNull(workflowService.currentTask(user));

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);
        assertNotNull(workflowService.currentTask(user));

        List<Task> fileDropTasks = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(1, fileDropTasks.size());

        assertEquals("addRecipients", fileDropTasks.get(0).getName());

        assertEquals("anonymous", fileDropTasks.get(0).getAssignee());

        taskService.complete(fileDropTasks.get(0).getId(), args);

        fileDropTasks = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(1, fileDropTasks.size());

        assertEquals("addFiles", fileDropTasks.get(0).getName());

        taskService.complete(fileDropTasks.get(0).getId());

        fileDropTasks = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(0, fileDropTasks.size());
        assertNull(workflowService.getCurrentTask(user));
    }

    @Test
    public void startProcessMultipleAnonymous() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);
        assertNull(workflowService.currentTask(user));

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);
        assertNotNull(workflowService.currentTask(user));

        AnonymousUser anotherUser = new AnonymousUser();
        assertNotNull(anotherUser);

        Map<String, Object> args2 = new HashMap<>();
        args2.put("initiator", anotherUser.getUsername());

        ProcessInstance process2 = runtimeService.startProcessInstanceByKey("fileUpload", args2);
        assertNotNull(process2);

        List<Task> userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(2, userTasks.size());

        List<Task> processTask = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(1, processTask.size());

        runtimeService.deleteProcessInstance(process.getId(), "test");
        runtimeService.deleteProcessInstance(process2.getId(), "test");
        assertNull(workflowService.currentTask(user));
    }

    @Test
    @WithMockUhUser
    public void startProcessUH() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        assertNull(workflowService.currentTask(user));

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);
        assertNotNull(workflowService.currentTask(user));

        List<Task> userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(1, userTasks.size());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals("addRecipients", userTasks.get(0).getName());

        taskService.complete(userTasks.get(0).getId());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals("addFiles", userTasks.get(0).getName());

        taskService.complete(userTasks.get(0).getId());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(0, userTasks.size());
        assertNull(workflowService.currentTask(user));
    }
}
