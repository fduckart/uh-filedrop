package edu.hawaii.its.filedrop.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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

    @Autowired
    private LdapService ldapService;

    @Test
    @WithMockUhUser
    public void testWithService() {
        User user = userContextService.getCurrentUser();
        workflowService.stopProcess(user);
        assertNotNull(user);

        assertNull(workflowService.getCurrentTask(user));
        fileDropService.startUploadProcess(user);
        assertNotNull(workflowService.getCurrentTask(user));

        assertEquals("addRecipients", workflowService.getCurrentTask(user).getName());

        String[] recipients = { "test" };
        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null, null, null);
        fileDropService.completeFileDrop(user, null);

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

        fileDropService.uploadFile(user, null, null, null); // Doesn't work since on recipientsTask.

        fileDropService.addRecipients(user, recipients);

        assertEquals("addFiles", workflowService.getCurrentTask(user).getName());
        fileDropService.uploadFile(user, null, null, null);
        fileDropService.completeFileDrop(user, null);
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
    public void processVariablesStringTest() {
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
    public void processVariablesTest() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);

        fileDropService.startUploadProcess(user);

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "123");
        workflowService.addProcessVariables(workflowService.getCurrentTask(user), variables);

        Map<String, Object> taskVariables =
                workflowService.getProcessVariables(workflowService.getCurrentTask(user));

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
        workflowService.addProcessVariables(
                workflowService.getCurrentTask(user).getProcessInstanceId(), args);

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
        assertEquals(2,
                fileDropService.findFileSets(fileDropService.findFileDrop((Integer) vars.get("fileDropId"))).size());
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
        workflowService.addProcessVariables(workflowService.getCurrentTask(user).getProcessInstanceId(), args);

        fileDropService.addRecipients(user, recipients);

        assertEquals("filesTask", workflowService.getCurrentTask(user).getTaskDefinitionKey());

        workflowService.revertTask(user, "recipientsTask");

        assertEquals("recipientsTask", workflowService.getCurrentTask(user).getTaskDefinitionKey());
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
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("test@some.edu")));
    }

    @Test
    @WithMockUhUser(affiliation = "staff")
    public void checkRecipientStaffTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("test@some.edu")));
    }

    @Test
    @WithMockUhUser(affiliation = "student")
    public void checkRecipientStudentTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("test@some.edu")));
    }

    @Test
    @WithMockUhUser(affiliation = "affiliate")
    public void checkRecipientAffiliateTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("test@some.edu")));
    }

    @Test
    @WithMockUhUser(affiliation = "other")
    public void checkRecipientOtherTest() {
        User user = userContextService.getCurrentUser();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setAuthenticationRequired(true);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));

        fileDrop.setAuthenticationRequired(false);
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("user")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("beno")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("krichards")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("help")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("uhmfund")));
        assertFalse(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("teststudent")));
        assertTrue(fileDropService.checkRecipient(user, fileDrop, ldapService.findByUhUuidOrUidOrMail("test@some.edu")));
    }
}
