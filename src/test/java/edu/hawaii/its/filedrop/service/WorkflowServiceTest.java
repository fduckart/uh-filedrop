package edu.hawaii.its.filedrop.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
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
        workflowService.stopProcessAll(user);
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
        workflowService.stopProcessAll(user);
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
        workflowService.stopProcessAll(user);

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

    @Test
    public void stopProcessAll() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);
        workflowService.stopProcessAll(user);
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

        workflowService.stopProcessAll(user);

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();
        assertEquals(0, userTasks.size());

        // Just to make sure it doesn't barf.
        workflowService.stopProcessAll(user);

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();
        assertEquals(0, userTasks.size());

        userTasks = taskService.createTaskQuery().taskAssignee("gonzo").list();
        assertEquals(0, userTasks.size());
    }

    @Test
    @WithMockUhUser
    public void specialCharacters() {
        User user = userContextService.getCurrentUser();
        workflowService.stopProcessAll(user);

        final String s0 = "Kahakō in Hā Kūpuna";
        final String s1 = "Kahakō";
        final String s2 = "ʻokina";

        Map<String, Object> map = new HashMap<>();
        map.put("s0", s0);
        map.put("i0", new Integer(0));
        map.put("s1", s1);
        map.put("i1", new Integer(1));
        map.put("s2", s2);
        map.put("i2", new Integer(2));

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);
        assertNotNull(workflowService.currentTask(user));

        Task task = workflowService.getCurrentTask(user);
        assertNotNull(task);

        workflowService.addProcessVariables(task, map);

        Map<String, Object> result = workflowService.getProcessVariables(process.getId());

        assertThat(result.get("s0"), equalTo(s0));
        assertThat(result.get("s1"), equalTo(s1));
        assertThat(result.get("s2"), equalTo(s2));

        assertThat(result.get("i0"), equalTo(Integer.valueOf(0)));
        assertThat(result.get("i1"), equalTo(Integer.valueOf(1)));
        assertThat(result.get("i2"), equalTo(Integer.valueOf(2)));

        runtimeService.deleteProcessInstance(process.getId(), "test");
    }

}
