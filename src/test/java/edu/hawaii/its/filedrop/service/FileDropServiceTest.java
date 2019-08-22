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
import edu.hawaii.its.filedrop.access.UserBuilder;
import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.FileSetRepository;
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
    private UserBuilder userBuilder;

    @Autowired
    private FileSetRepository fileSetRepository;

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
    }

    @Test
    public void startProcessUH() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhuuid", "12345678");

        User user = userBuilder.make(map);
        assertNotNull(user);

        Map<String, Object> args = new HashMap<>();
        args.put("initiator", user.getUsername());

        ProcessInstance process = runtimeService.startProcessInstanceByKey("fileUpload", args);
        assertNotNull(process);

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

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

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
    public void addNoRecipients() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "anotherUser");
        map.put("uhuuid", "910111213");

        User user = userBuilder.make(map);
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = new String[0];
        fileDropService.addRecipients(user, recipients);

        Map<String, Object> processVariables =
                fileDropService.getProcessVariables(fileDropService.getCurrentTask(user).getProcessInstanceId());

        assertFalse(processVariables.isEmpty());
        assertEquals(2, processVariables.size());
        assertTrue(processVariables.containsKey("recipients"));
        assertEquals("anotherUser", ((String[]) processVariables.get("recipients"))[0]);
    }

    @Test
    public void addRecipientsWithoutProcess() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "uhUser");
        map.put("uhuuid", "9900992");

        User user = userBuilder.make(map);
        assertNotNull(user);

        String[] recipients = new String[0];
        fileDropService.addRecipients(user, recipients);

        assertNull(fileDropService.getCurrentTask(user));
    }

    @Test
    public void addRecipientsTwice() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "newestUser");
        map.put("uhuuid", "9909992");

        User user = userBuilder.make(map);
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = new String[0];

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", fileDropService.getCurrentTask(user).getName());

    }

    @Test
    public void processVariablesTest() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "newUser");
        map.put("uhuuid", "9999992");

        User user = userBuilder.make(map);
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
    public void taskVariablesTest() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "testUser");
        map.put("uhuuid", "9999999");

        User user = userBuilder.make(map);
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
    public void fileDropTest() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "aUhUser");
        map.put("uhuuid", "1234567");

        User user = userBuilder.make(map);
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = { "test", "lukemcd9" };

        FileDrop fileDrop = new FileDrop();
        fileDrop.setUploader(user.getUid());
        fileDrop.setUploaderFullName("A Uh User");
        fileDrop.setUploadKey("test-ul-key");
        fileDrop.setDownloadKey("test-dl-key");
        fileDrop.setEncryptionKey("test-enc-key");
        fileDrop.setRecipient(Arrays.toString(recipients));
        fileDrop.setValid(true);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setCreated(LocalDate.now());
        fileDrop.setExpiration(LocalDate.now().plus(10, ChronoUnit.DAYS));
        fileDropService.saveFileDrop(fileDrop);

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
