package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.flowable.task.api.Task;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class FileDropServiceTest {

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private LdapService ldapService;

    @Test
    @WithMockUhUser
    public void testWithService() {
        User user = userContextService.getCurrentUser();
        workflowService.stopProcess(user);
        assertNotNull(user);

        assertNull(currentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(currentTask(user));

        assertEquals("addRecipients", currentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", currentTask(user).getName());
        fileDropService.uploadFile(user, null, null, null);
        fileDropService.completeFileDrop(user, null);

        assertNull(currentTask(user));
    }

    @Test
    @WithMockUhUser
    public void testMultipleProcess() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        workflowService.stopProcess(user);
        assertNull(currentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(currentTask(user));

        assertEquals("addRecipients", currentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", currentTask(user).getName());

        fileDropService.startUploadProcess(user);

        assertEquals("addRecipients", currentTask(user).getName());

        fileDropService.uploadFile(user, null, null, null); // Doesn't work since on recipientsTask.

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", currentTask(user).getName());
        fileDropService.uploadFile(user, null, null, null);
        fileDropService.completeFileDrop(user, null);
        assertNull(currentTask(user));
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
                workflowService.getProcessVariables(currentTask(user));

        assertFalse(processVariables.isEmpty());
        assertEquals(2, processVariables.size());
        assertTrue(processVariables.containsKey("recipients"));
    }

    @Test
    @WithMockUhUser
    public void addRecipientsWithoutProcess() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        String[] recipients = new String[0];
        fileDropService.addRecipients(user, recipients);

        assertNull(currentTask(user));
    }

    @Test
    @WithMockUhUser
    public void addRecipientsTwice() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String[] recipients = new String[0];

        fileDropService.addRecipients(user, recipients);
        assertEquals("addFiles", currentTask(user).getName());

        fileDropService.addRecipients(user, recipients);
        assertEquals("addFiles", currentTask(user).getName());
    }

    @Test
    @WithMockUhUser
    public void processVariablesStringTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        String processId = currentTask(user).getProcessInstanceId();
        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "123");
        workflowService.addProcessVariables(processId, variables);

        processId = currentTask(user).getProcessInstanceId();
        Map<String, Object> processVariables = workflowService.getProcessVariables(processId);

        assertFalse(processVariables.isEmpty());
        assertEquals(2, processVariables.size());
        assertEquals("123", processVariables.get("test"));
    }

    @Test
    @WithMockUhUser(username = "rstarr", uhuuid = "9887722")
    public void processVariablesTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "123");
        workflowService.addProcessVariables(currentTask(user), variables);

        Map<String, Object> taskVariables =
                workflowService.getProcessVariables(currentTask(user));

        assertFalse(taskVariables.isEmpty());
        assertEquals(2, taskVariables.size());
        assertEquals("123", taskVariables.get("test"));
    }

    @Test
    public void getFileDropTest() {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setDownloadKey("test-key");
        fileDrop.setUploadKey("test-key2");

        fileDropService.saveFileDrop(fileDrop);

        assertEquals(fileDrop.getId(), fileDropService.findFileDropDownloadKey("test-key").getId());
        assertEquals(fileDrop.getId(), fileDropService.findFileDropUploadKey("test-key2").getId());
    }

    @Test
    public void findFileDrop() {
        FileDrop fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop, not(equalTo(null)));
        assertThat(fileDrop.getId(), equalTo(1));

        fileDrop = fileDropService.findFileDrop(0);
        assertThat(fileDrop, equalTo(null));

        fileDrop = fileDropService.findFileDrop(null);
        assertThat(fileDrop, equalTo(null));

        fileDrop = fileDropService.findFileDrop(-1);
        assertThat(fileDrop, equalTo(null));
    }

    @Test
    public void findFileDropDownloadKey() {
        String downloadKey = "downloadKey3";
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);
        assertThat(fileDrop.getDownloadKey(), equalTo(downloadKey));
        assertThat(fileDrop.isValid(), equalTo(true));
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));

        downloadKey = "downloadKey2";
        fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);
        assertThat(fileDrop.getDownloadKey(), equalTo(downloadKey));
        assertThat(fileDrop.isValid(), equalTo(true));
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
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
        fileDrop.setValid(true);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileDropService.addRecipients(fileDrop, recipients);

        Map<String, Object> args = new HashMap<>();
        args.put("fileDropId", fileDrop.getId());
        workflowService.addProcessVariables(currentTask(user).getProcessInstanceId(), args);

        assertTrue(workflowService.hasFileDrop(user));

        fileDropService.addRecipients(user, recipients);

        FileSet fileSet = new FileSet();
        fileSet.setFileName("test.png");
        fileSet.setType("image/png");
        fileSet.setComment("Test image png");

        Map<String, Object> vars = workflowService.getProcessVariables(user);

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
        assertEquals(2, fileDropService.findFileSets(fileDropService.findFileDrop((Integer) vars.get("fileDropId"))).size());
        assertNotEquals(fileDropService.findAllFileDrop().size(), 0);
    }

    @Test
    public void revertToTask() {
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
        fileDrop.setValid(true);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileDropService.addRecipients(fileDrop, recipients);

        Map<String, Object> args = new HashMap<>();
        args.put("fileDropId", fileDrop.getId());
        workflowService.addProcessVariables(currentTask(user).getProcessInstanceId(), args);

        fileDropService.addRecipients(user, recipients);

        assertEquals("filesTask", currentTask(user).getTaskDefinitionKey());

        workflowService.revertTask(user, "recipientsTask");

        assertEquals("recipientsTask", currentTask(user).getTaskDefinitionKey());
    }

    @Test
    public void startUploadProcessNullCheck() {
        try {
            fileDropService.startUploadProcess(null);
            fail("Not reaching here, but might change this behavior.");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void deleteUploadProcessNullCheck() {
        fileDropService.deleteUploadProcess(null);
    }

    @Test
    @WithMockUhUser
    public void deleteUploadProcess() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.deleteUploadProcess(user);

        fileDropService.startUploadProcess(user);

        Map<String, Object> args = new HashMap<>();
        args.put("fileDropId", Integer.MIN_VALUE);
        String processId = currentTask(user).getProcessInstanceId();
        workflowService.addProcessVariables(processId, args);

        fileDropService.deleteUploadProcess(user);
    }

    @Test
    public void testToShortString() {
        FileDrop fileDrop = new FileDrop();
        assertThat(fileDrop.toStringShort(), startsWith("FileDrop ["));
        assertThat(fileDrop.toStringShort(), containsString("[id=null, "));
        assertThat(fileDrop.toStringShort(), containsString(", recipients=null"));
        assertThat(fileDrop.toStringShort(), containsString(", created=null"));
        assertThat(fileDrop.toStringShort(), containsString(", expiration=null"));
        assertThat(fileDrop.toStringShort(), containsString(", valid=null"));
        assertThat(fileDrop.toStringShort(), endsWith("]"));
    }

    @Test
    @WithMockUhUser(affiliation = "faculty")
    public void checkRecipientFacultyTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("test@some.edu"), fileDrop.isAuthenticationRequired()));
    }

    @Test
    @WithMockUhUser(affiliation = "staff")
    public void checkRecipientStaffTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("test@some.edu"), fileDrop.isAuthenticationRequired()));
    }

    @Test
    @WithMockUhUser(affiliation = "student")
    public void checkRecipientStudentTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("test@some.edu"), fileDrop.isAuthenticationRequired()));
    }

    @Test
    @WithMockUhUser(affiliation = "affiliate")
    public void checkRecipientAffiliateTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("test@some.edu"), fileDrop.isAuthenticationRequired()));
    }

    @Test
    @WithMockUhUser(affiliation = "other")
    public void checkRecipientOtherTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));

        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("user"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("beno"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("krichards"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("help"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("uhmfund"), fileDrop.isAuthenticationRequired()));
        assertFalse(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("teststudent"), fileDrop.isAuthenticationRequired()));
        assertTrue(fileDropService.checkRecipient(user, findByUhUuidOrUidOrMail("test@some.edu"), fileDrop.isAuthenticationRequired()));
    }

    @Test
    public void containsRecipient() {
        FileDrop fileDrop = fileDropService.findFileDrop(3);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(2);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(4);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon@hawaii.edu"), equalTo(true));
    }

    @Test
    public void isAuthorizedOne() {
        FileDrop fileDrop = fileDropService.findFileDrop(3);
        assertThat(fileDrop.getUploader(), equalTo("jwlennon@hawaii.edu"));
        assertThat(fileDrop.getRecipients().size(), equalTo(1));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards@example.com"), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, "jwlennon@hawaii.edu"), equalTo(true));
        assertThat(fileDropService.isAuthorized(fileDrop, "jwlennon@HAWAII.EDU"), equalTo(false)); // Note.
        assertThat(fileDropService.isAuthorized(fileDrop, "jwlennon"), equalTo(false)); // Note.
        assertThat(fileDropService.isAuthorized(fileDrop, "krichards@example.com"), equalTo(true));

        fileDrop = fileDropService.findFileDrop(2);
        assertThat(fileDrop.getUploader(), equalTo("test"));
        assertThat(fileDrop.getRecipients().size(), equalTo(1));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.isAuthorized(fileDrop, "TEST"), equalTo(true));
        assertThat(fileDropService.isAuthorized(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop.getUploader(), equalTo("test"));
        assertThat(fileDrop.getRecipients().size(), equalTo(1));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.isAuthorized(fileDrop, "TEST"), equalTo(true));
        assertThat(fileDropService.isAuthorized(fileDrop, "duckart"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(4);
        assertThat(fileDrop.getUploader(), equalTo("jwlennon@hawaii.edu"));
        assertThat(fileDrop.getRecipients().size(), equalTo(2));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon@hawaii.edu"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards@example.com"), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, "krichards@example.com"), equalTo(true));
        assertThat(fileDropService.isAuthorized(fileDrop, "jwlennon@hawaii.edu"), equalTo(true));

        // Misc checks.
        assertThat(fileDropService.isAuthorized(fileDrop, null), equalTo(false));
        assertThat(fileDropService.isAuthorized(fileDrop, ""), equalTo(false));
        assertThat(fileDropService.isAuthorized(fileDrop, " "), equalTo(false));
    }

    private Task currentTask(User user) {
        return workflowService.getCurrentTask(user);
    }

    private LdapPerson findByUhUuidOrUidOrMail(String value) {
        return ldapService.findByUhUuidOrUidOrMail(value);
    }
}
