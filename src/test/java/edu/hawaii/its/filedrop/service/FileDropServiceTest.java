package edu.hawaii.its.filedrop.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
    private WorkflowService workflowService;

    @Autowired
    private UserContextService userContextService;

    @Test
    @WithMockUhUser
    public void testWithService() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        assertNull(workflowService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(workflowService.getCurrentTask(user));

        assertEquals("addRecipients", workflowService.getCurrentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null);

        assertNull(workflowService.getCurrentTask(user));
    }

    @Test
    @WithMockUhUser
    public void testMultipleProcess() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        workflowService.stopProcess(user);
        assertNull(workflowService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(workflowService.getCurrentTask(user));

        assertEquals("addRecipients", workflowService.getCurrentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());

        fileDropService.startUploadProcess(user);

        assertEquals("addRecipients", workflowService.getCurrentTask(user).getName());

        fileDropService.uploadFile(user, null); // Doesn't work since on recipientsTask.

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null);

        assertNull(workflowService.getCurrentTask(user));
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
                workflowService.getProcessVariables(workflowService.getCurrentTask(user).getProcessInstanceId());

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

        assertNull(workflowService.getCurrentTask(user));
    }

    @Test
    @WithMockUhUser
    public void addRecipientsTwice() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = new String[0];

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());

    }

    @Test
    @WithMockUhUser
    public void processVariablesTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "123");
        workflowService
                .addProcessVariables(workflowService.getCurrentTask(user).getProcessInstanceId(), variables);

        Map<String, Object> processVariables =
                workflowService
                        .getProcessVariables(workflowService.getCurrentTask(user).getProcessInstanceId());

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
        workflowService.addTaskVariables(workflowService.getCurrentTask(user).getId(), variables);

        Map<String, Object> taskVariables =
                workflowService.getTaskVariables(workflowService.getCurrentTask(user).getId());

        assertFalse(taskVariables.isEmpty());
        assertEquals(2, taskVariables.size());
        assertEquals("123", taskVariables.get("test"));
    }

    @Test
    public void getFileDropTest() {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setDownloadKey("test-key");

        fileDropService.saveFileDrop(fileDrop);

        assertEquals(fileDrop.getId(), fileDropService.findFileDrop("test-key").getId());
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
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        Map<String, Object> args = new HashMap<>();
        args.put("fileDropId", fileDrop.getId());
        workflowService.addProcessVariables(
                workflowService.getCurrentTask(user).getProcessInstanceId(), args
        );

        fileDropService.addRecipients(user, recipients);

        FileSet fileSet = new FileSet();
        fileSet.setFileName("test.png");
        fileSet.setType("image/png");
        fileSet.setComment("Test image png");

        Map<String, Object> vars =
                workflowService.getProcessVariables(workflowService.getCurrentTask(user).getProcessInstanceId());

        fileSet.setFileDrop(fileDropService.findFileDrop((Integer) vars.get("fileDropId")));

        fileDropService.saveFileSet(fileSet);

        fileSet = new FileSet();
        fileSet.setFileName("test.jpg");
        fileSet.setType("image/jpg");
        fileSet.setComment("Test image jpg");

        fileSet.setFileDrop(fileDropService.findFileDrop((Integer) vars.get("fileDropId")));

        fileDropService.saveFileSet(fileSet);

        assertEquals(2, fileSet.getId().intValue());

        assertEquals(fileDrop.getId(), fileSet.getFileDrop().getId());
        assertEquals(2,
                fileDropService.findFileSets(fileDropService.findFileDrop((Integer) vars.get("fileDropId"))).size());
    }
}
