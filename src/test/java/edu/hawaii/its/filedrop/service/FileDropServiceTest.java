package edu.hawaii.its.filedrop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.access.AnonymousUser;
import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserBuilder;
import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
public class FileDropServiceTest {

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserBuilder userBuilder;

    @Autowired
    private IdentityService identityService;

    @Test
    public void startProcessTest() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("sender", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

        List<Task> fileDropTasks = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(1, fileDropTasks.size());

        assertEquals("Add Recipients", fileDropTasks.get(0).getName());

        assertEquals("anonymous", fileDropTasks.get(0).getAssignee());

        taskService.complete(fileDropTasks.get(0).getId(), args);

        fileDropTasks = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(1, fileDropTasks.size());

        assertEquals("Add Files", fileDropTasks.get(0).getName());

        taskService.complete(fileDropTasks.get(0).getId());

        fileDropTasks = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(0, fileDropTasks.size());

    }

    @Test
    public void startProcessMultipleAnonymous() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("sender", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

        AnonymousUser anotherUser = new AnonymousUser();
        assertNotNull(anotherUser);

        Map<String, Object> args2 = new HashMap<>();
        args2.put("sender", anotherUser.getUsername());

        ProcessInstance process2 = runtimeService.startProcessInstanceByKey("fileUpload", args2);
        assertNotNull(process2);

        List<Task> userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(2, userTasks.size());

        List<Task> processTask = taskService.createTaskQuery().processInstanceId(process.getId()).list();

        assertEquals(1, processTask.size());
    }

    @Test
    public void startProcessUH() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhuuid", "12345678");

        User user = userBuilder.make(map);
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("sender", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

        List<Task> userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(1, userTasks.size());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals("Add Recipients", userTasks.get(0).getName());

        taskService.complete(userTasks.get(0).getId());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals("Add Files", userTasks.get(0).getName());

        taskService.complete(userTasks.get(0).getId());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(0, userTasks.size());
    }

    @Test
    public void testWithService() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhuuid", "12345678");

        User user = userBuilder.make(map);
        assertNotNull(user);

        assertNull(fileDropService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(fileDropService.getCurrentTask(user));

        assertEquals("addRecipients", fileDropService.getCurrentTask(user).getName());

        String[] recipients = {"test"};
        fileDropService.addRecipient(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null);

        assertNull(fileDropService.getCurrentTask(user));
    }

    @Test
    public void testMultipleProcess() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhuuid", "12345678");

        User user = userBuilder.make(map);
        assertNotNull(user);

        assertNull(fileDropService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(fileDropService.getCurrentTask(user));

        assertEquals("addRecipients", fileDropService.getCurrentTask(user).getName());

        String[] recipients = {"test"};
        fileDropService.addRecipient(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());

        fileDropService.startUploadProcess(user);

        assertEquals("addRecipients", fileDropService.getCurrentTask(user).getName());

        fileDropService.uploadFile(user, null); // Doesn't work since on recipientsTask.

        fileDropService.addRecipient(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null);

        assertNull(fileDropService.getCurrentTask(user));
    }
}
