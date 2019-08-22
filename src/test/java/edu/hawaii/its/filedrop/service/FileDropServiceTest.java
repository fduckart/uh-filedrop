package edu.hawaii.its.filedrop.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.access.AnonymousUser;
import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.controller.WithMockUhUser;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class FileDropServiceTest {

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserContextService userContextService;

    @Test
    public void startProcessTest() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

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

    }

    @Test
    public void startProcessMultipleAnonymous() {
        AnonymousUser user = new AnonymousUser();
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

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
    }

    @Test
    @WithMockUhUser
    public void startProcessUH() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        List<Task> userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        for (Task task : userTasks) {
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "test");
        }

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

        assertEquals(1, userTasks.size());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals("addRecipients", userTasks.get(0).getName());

        taskService.complete(userTasks.get(0).getId());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals("addFiles", userTasks.get(0).getName());

        taskService.complete(userTasks.get(0).getId());

        userTasks = taskService.createTaskQuery().taskAssignee(user.getUsername()).list();

        assertEquals(0, userTasks.size());
    }

    @Test
    @WithMockUhUser
    public void testWithService() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        assertNull(fileDropService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(fileDropService.getCurrentTask(user));

        assertEquals("addRecipients", fileDropService.getCurrentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null);

        assertNull(fileDropService.getCurrentTask(user));
    }

    @Test
    @WithMockUhUser
    public void testMultipleProcess() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        runtimeService.deleteProcessInstance(fileDropService.getCurrentTask(user).getProcessInstanceId(), "test");
        assertNull(fileDropService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(fileDropService.getCurrentTask(user));

        assertEquals("addRecipients", fileDropService.getCurrentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());

        fileDropService.startUploadProcess(user);

        assertEquals("addRecipients", fileDropService.getCurrentTask(user).getName());

        fileDropService.uploadFile(user, null); // Doesn't work since on recipientsTask.

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null);

        assertNull(fileDropService.getCurrentTask(user));
    }

    @Test
    @WithMockUhUser
    public void addNoRecipients() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = new String[0];
        fileDropService.addRecipients(user, recipients);

        Map<String, Object> processVariables =
                fileDropService.getProcessVariables(fileDropService.getCurrentTask(user).getProcessInstanceId());

        assertFalse(processVariables.isEmpty());
        assertEquals(2, processVariables.size());
        assertTrue(processVariables.containsKey("recipients"));
        assertEquals("user", ((String[]) processVariables.get("recipients"))[0]);
    }

    @Test
    @WithMockUhUser
    public void addRecipientsWithoutProcess() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        String[] recipients = new String[0];
        fileDropService.addRecipients(user, recipients);

        assertNull(fileDropService.getCurrentTask(user));
    }

    @Test
    @WithMockUhUser
    public void addRecipientsTwice() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = new String[0];

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());

    }

    @Test
    @WithMockUhUser
    public void processVariablesTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "123");
        fileDropService
                .addProcessVariables(fileDropService.getCurrentTask(user).getProcessInstanceId(), variables);

        Map<String, Object> processVariables =
                fileDropService
                        .getProcessVariables(fileDropService.getCurrentTask(user).getProcessInstanceId());

        assertFalse(processVariables.isEmpty());
        assertEquals(2, processVariables.size());
        assertEquals("123", processVariables.get("test"));
    }

    @Test
    @WithMockUhUser(username = "rstarr", uhuuid = "9887722")
    public void taskVariablesTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "123");
        fileDropService.addTaskVariables(fileDropService.getCurrentTask(user).getId(), variables);

        Map<String, Object> taskVariables =
                fileDropService.getTaskVariables(fileDropService.getCurrentTask(user).getId());

        assertFalse(taskVariables.isEmpty());
        assertEquals(2, taskVariables.size());
        assertEquals("123", taskVariables.get("test"));
    }

    @Test
    public void getFileDropTest() {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setDownloadKey("test-key");

        fileDropService.saveFileDrop(fileDrop);

        assertEquals(fileDrop.getId(), fileDropService.getFileDrop("test-key").getId());
    }

    @Test
    @WithMockUhUser
    public void fileDropTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = { "test", "lukemcd9" };

        FileDrop fileDrop = new FileDrop();
        fileDrop.setUploader(user.getUid());
        fileDrop.setUploaderFullName(user.getName());
        fileDrop.setUploadKey("test-ul-key");
        fileDrop.setDownloadKey("test-dl-key");
        fileDrop.setEncryptionKey("test-enc-key");
        fileDrop.setRecipient(Arrays.toString(recipients));
        fileDrop.setValid(true);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setCreated(LocalDate.now());
        fileDrop.setExpiration(LocalDate.now().plus(10, ChronoUnit.DAYS));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        Map<String, Object> args = new HashMap<>();
        args.put("fileDropId", fileDrop.getId());
        fileDropService.addProcessVariables(fileDropService.getCurrentTask(user).getProcessInstanceId(), args);

        fileDropService.addRecipients(user, recipients);

        FileSet fileSet = new FileSet();
        fileSet.setFileName("test.png");
        fileSet.setType("image/png");
        fileSet.setComment("Test image png");

        Map<String, Object> vars =
                fileDropService.getProcessVariables(fileDropService.getCurrentTask(user).getProcessInstanceId());

        fileSet.setFileDrop(fileDropService.getFileDrop((Integer) vars.get("fileDropId")));

        fileDropService.saveFileSet(fileSet);

        fileSet = new FileSet();
        fileSet.setFileName("test.jpg");
        fileSet.setType("image/jpg");
        fileSet.setComment("Test image jpg");

        fileSet.setFileDrop(fileDropService.getFileDrop((Integer) vars.get("fileDropId")));

        fileDropService.saveFileSet(fileSet);

        assertEquals(2, fileSet.getId().intValue());

        assertEquals(fileDrop.getId(), fileSet.getFileDrop().getId());
        assertEquals(2,
                fileDropService.getFileSets(fileDropService.getFileDrop((Integer) vars.get("fileDropId"))).size());
    }
}
