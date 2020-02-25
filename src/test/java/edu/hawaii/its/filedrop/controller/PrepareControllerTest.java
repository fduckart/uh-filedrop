package edu.hawaii.its.filedrop.controller;

import java.util.List;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class PrepareControllerTest {

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmailService emailService;

    private MockMvc mockMvc;

    @Rule
    public GreenMailRule server = new GreenMailRule(new ServerSetup(1025, "localhost", "smtp"));

    @Before
    public void construct() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        emailService.setEnabled(false);
    }

    @Test
    @WithMockUhUser
    public void addRecipientsTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "jwlennon@hawaii.edu", "test2")
                .param("validation", "false")
                .param("expiration", "5")
                .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(8);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("John W Lennon", "test2")));

        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "jwlennon@hawaii.edu", "test2")
                .param("validation", "false")
                .param("expiration", "5")
                .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("John W Lennon", "test2")));

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));
    }

    @Test
    @WithMockUhUser
    public void addRecipients() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "jwlennon@hawaii.edu")
                .param("recipients", "test", "jwlennon")
                .param("validation", "true")
                .param("expiration", "5")
                .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(6);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("test", "John W Lennon")));

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));
    }

    @Test
    @WithMockUhUser(username = "jwlennon", name = "John W Lennon", email = "jwlennon@hawaii.edu")
    public void addNoRecipients() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "jwlennon@hawaii.edu")
                .param("recipients", "")
                .param("validation", "true")
                .param("expiration", "5")
                .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(3);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("test2@test.com")));

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));
    }

    @Test
    @WithMockUhUser
    public void taskRedirectTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(get("/prepare/files/uploadKey"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));
    }

    @Test
    @WithMockUhUser(username = "jwlennon", name = "John W Lennon", email = "jwlennon@hawaii.edu")
    public void addFilesTest() throws Exception {
        emailService.setEnabled(true);

        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "jwlennon@hawaii.edu")
                .param("recipients", "krichards@example.com")
                .param("validation", "true")
                .param("expiration", "5")
                .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(3);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/prepare/files/" + fileDrop.getUploadKey())
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());
        assertNotNull(fileDrop);

        List<FileSet> fileSets = fileDropService.findFileSets(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(2, fileSets.size());
        assertEquals("test.txt", fileSets.get(0).getFileName());
        assertEquals("text/plain", fileSets.get(0).getType());
        assertEquals("test comment", fileSets.get(0).getComment());

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));

        MimeMessage[] receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(2));
        assertThat(receivedMessages[1].getAllRecipients()[0].toString(), equalTo("test2@test.com"));
        assertThat(receivedMessages[1].getFrom()[0].toString(), equalTo("jwlennon@hawaii.edu"));
        assertThat(receivedMessages[1].getContent().toString(), containsString("jwlennon@hawaii.edu"));
    }

    @Test
    @WithMockUhUser(username = "jwlennon", name = "John W Lennon", email = "jwlennon@hawaii.edu")
    public void addFilesNonUhTest() throws Exception {
        emailService.setEnabled(true);

        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "jwlennon@hawaii.edu")
                .param("recipients", "test2@test.com")
                .param("validation", "false")
                .param("expiration", "5")
                .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop filedrop = fileDropService.findFileDrop(3);

        mockMvc.perform(get("/prepare/files/" + filedrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/prepare/files/" + filedrop.getUploadKey())
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        FileDrop fileDrop = fileDropService.findFileDrop(3);
        assertNotNull(fileDrop);

        List<FileSet> fileSets = fileDropService.findFileSets(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());
        assertEquals("test.txt", fileSets.get(0).getFileName());
        assertEquals("text/plain", fileSets.get(0).getType());
        assertEquals("test comment", fileSets.get(0).getComment());

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));

        MimeMessage[] receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(2));
        assertThat(receivedMessages[1].getAllRecipients()[0].toString(), equalTo("test2@test.com"));
        assertThat(receivedMessages[1].getFrom()[0].toString(), equalTo("jwlennon@hawaii.edu"));
        assertThat(receivedMessages[1].getContent().toString(), containsString("jwlennon@hawaii.edu"));
    }

    @Test
    @WithMockUhUser
    public void completeNotUploaderTest() throws Exception {
        FileDrop fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop.getUploader(), not(equalTo("user")));
        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));
    }

    @Test
    public void helpdeskTest() throws Exception {
        mockMvc.perform(get("/helpdesk"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare-helpdesk"));

        mockMvc.perform(post("/helpdesk")
                .param("sender", "Test")
                .param("expiration", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/helpdesk/files/{downloadKey}"));

        FileDrop fileDrop =
                fileDropRepository.findAll().stream().filter(fd -> fd.getUploader().equals("Test"))
                        .findFirst().orElse(null);

        mockMvc.perform(get("/helpdesk/files/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("downloadKey", "maxUploadSize", "recipients"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/helpdesk/files/" + fileDrop.getDownloadKey())
                .file(mockMultipartFile)
                .param("comment", "test")
                .param("expiration", "30"))
                .andExpect(status().isOk());

        List<FileSet> fileSets = fileDropService.findFileSets(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());
        assertEquals("test.txt", fileSets.get(0).getFileName());
        assertEquals("text/plain", fileSets.get(0).getType());
        assertEquals("test", fileSets.get(0).getComment());

        mockMvc.perform(get("/helpdesk/successful"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }
}
