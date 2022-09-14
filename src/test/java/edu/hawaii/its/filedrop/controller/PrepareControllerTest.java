package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import javax.mail.internet.MimeMessage;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.repository.FileSetRepository;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.util.Strings;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class PrepareControllerTest {

    @RegisterExtension
    static final GreenMailExtension emailServer = new GreenMailExtension(ServerSetupTest.SMTP);

    @Value("${app.mail.help}")
    private String helpName;

    @Value("${app.mail.to.help}")
    private String helpEmail;

    @Value("${app.max.size}")
    private String maxUploadSize;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmailService emailService;

    private MockMvc mockMvc;

    @BeforeEach
    public void construct() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        emailService.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        emailService.setEnabled(false);
    }

    @Test
    public void helpDesk() {
        assertThat(helpName, equalTo("Frank R Duckart"));
        assertThat(helpEmail, equalTo("duckart@hawaii.edu"));
    }

    @Test
    @WithMockUhUser
    public void addRecipientsTest() throws Exception {
        mockMvc.perform(get("/prepare")
                        .param("expiration", "30"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("expiration"))
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "test")
                        .param("recipients", "jwlennon@hawaii.edu", "test2")
                        .param("validation", "false")
                        .param("expiration", "5")
                        .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(5);

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
                        .param("recipients", "jwlennon")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(5);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("John W Lennon")));

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
                .andExpect(model().attribute("recipients", empty()));

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

        long fileDropCount0 = fileDropRepository.count();
        long fileSetCount0 = fileSetRepository.count();

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

        long fileDropCount1 = fileDropRepository.count();
        assertThat(fileDropCount1, equalTo(fileDropCount0 + 1));
        long fileSetCount1 = fileSetRepository.count();
        assertThat(fileSetCount1, equalTo(fileSetCount0));

        Integer count = Long.valueOf(fileDropCount1).intValue();
        FileDrop fileDrop = fileDropService.findFileDrop(count);

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

        long fileDropCount2 = fileDropRepository.count();
        assertThat(fileDropCount2, equalTo(fileDropCount1));
        long fileSetCount2 = fileSetRepository.count();
        assertThat(fileSetCount2, equalTo(fileSetCount1 + 1));

        List<FileSet> fileSets = fileDropService.findFileSets(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());
        assertEquals("test.txt", fileSets.get(0).getFileName());
        assertEquals("text/plain", fileSets.get(0).getType());
        assertEquals("test comment", fileSets.get(0).getComment());

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));

        long fileDropCount3 = fileDropRepository.count();
        assertThat(fileDropCount3, equalTo(fileDropCount2));
        long fileSetCount3 = fileSetRepository.count();
        assertThat(fileSetCount3, equalTo(fileSetCount2));

        MimeMessage[] receivedMessages = emailServer.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(2));
        assertThat(receivedMessages[1].getAllRecipients()[0].toString(), equalTo("krichards@example.com"));
        assertThat(receivedMessages[1].getFrom()[0].toString(), equalTo("jwlennon@hawaii.edu"));
        assertThat(receivedMessages[1].getContent().toString(), containsString("jwlennon@hawaii.edu"));

        mockMvc.perform(get("/expire/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/expired"));

        long fileDropCount4 = fileDropRepository.count();
        assertThat(fileDropCount4, equalTo(fileDropCount3));
        long fileSetCount4 = fileSetRepository.count();
        assertThat(fileSetCount4, equalTo(fileSetCount3));
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

        Integer count = Long.valueOf(fileDropRepository.count()).intValue();
        FileDrop filedrop = fileDropService.findFileDrop(count);
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

        FileDrop fileDrop = fileDropService.findFileDrop(count);
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

        MimeMessage[] receivedMessages = emailServer.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(2));
        assertThat(receivedMessages[1].getAllRecipients()[0].toString(), equalTo("test2@test.com"));
        assertThat(receivedMessages[1].getFrom()[0].toString(), equalTo("jwlennon@hawaii.edu"));
        assertThat(receivedMessages[1].getContent().toString(), containsString("jwlennon@hawaii.edu"));

        mockMvc.perform(get("/expire/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/expired"));
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
    @WithMockUhUser
    public void helpdeskTest() throws Exception {
        mockMvc.perform(get("/helpdesk"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipient", "recipientEmail"))
                .andExpect(view().name("user/prepare-helpdesk"));

        System.out.println(Strings.fill('0', 59));

        mockMvc.perform(post("/helpdesk")
                        .param("sender", "Test")
                        .param("expiration", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/helpdesk/files/{uploadKey}"));

        System.out.println(Strings.fill('1', 59));

        mockMvc.perform(post("/helpdesk")
                        .param("sender", "Test")
                        .param("expiration", "30")
                        .param("ticketNumber", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/helpdesk/files/{uploadKey}"))
                .andDo(log()); // Set logger to DEBUG to see results.

        if ("off".equals("")) {

            FileDrop fileDrop =
                    fileDropRepository.findAll()
                            .stream()
                            .filter(fd -> fd.getUploader().equals("test"))
                            .findFirst()
                            .orElse(null);
            assertThat(fileDrop, notNullValue());
            assertThat(fileDrop.getUploadKey(), equalTo("uploadKey"));
            assertThat(fileDrop.getDownloadKey(), equalTo("downloadKey"));

            mockMvc.perform(get("/helpdesk/files/" + fileDrop.getUploadKey()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("uploadKey", "maxUploadSize", "recipients"))
                    .andExpect(model().attribute("uploadKey", is(equalTo("uploadKey"))))
                    .andExpect(model().attribute("maxUploadSize", is(equalTo(maxUploadSize))))
                    .andExpect(model().attribute("recipients", hasSize(1)))
                    .andExpect(model().attribute("recipients", hasItem("Frank R Duckart")));

            MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
                    "test.txt", "text/plain", "test data".getBytes());

            mockMvc.perform(multipart("/helpdesk/files/" + fileDrop.getUploadKey())
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

            mockMvc.perform(get("/helpdesk/successful/" + fileDrop.getUploadKey())
                            .param("expiration", "30")
                            .param("ticketNumber", "123456"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"));
        }
    }

    @Test
    @WithMockUhUser(affiliation = "faculty")
    public void restrictionsFacultyTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "krichards", "beno", "help"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files/")));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "uhmfund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "jwlennon")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("John W Lennon"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "teststudent")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Student"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test21")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Staff2"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "user")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test User"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add non-UH recipient when authentication is required."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").isEmpty());

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "help")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("ITS Help Desk"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "uhmfund")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));
    }

    @Test
    @WithMockUhUser
    public void restrictionsStaffTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "krichards", "beno", "help"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files/")));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "uhmfund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "jwlennon")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("John W Lennon"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "teststudent")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Student"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test21")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Staff2"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "user")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test User"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add non-UH recipient when authentication is required."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").isEmpty());

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "help")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("ITS Help Desk"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "uhmfund")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));
    }

    @Test
    @WithMockUhUser(affiliation = "student", username = "student")
    public void restrictionsStudentTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "krichards", "beno", "help"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "uhmfund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "jwlennon")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("John W Lennon"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "teststudent")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test21")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Staff2"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "user")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add non-UH recipient when authentication is required."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").isEmpty());

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "help")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("ITS Help Desk"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "uhmfund")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));
    }

    @Test
    @WithMockUhUser(affiliation = "affiliate", username = "affiliate")
    public void restrictionsAffiliateTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "krichards", "beno", "help"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "uhmfund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "jwlennon")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("John W Lennon"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "teststudent")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test21")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Staff2"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "user")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add non-UH recipient when authentication is required."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").isEmpty());

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "help")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("ITS Help Desk"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "uhmfund")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));
    }

    @Test
    @WithMockUhUser(affiliation = "other", username = "other")
    public void restrictionsOtherTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "krichards", "beno"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "help"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));

        mockMvc.perform(post("/prepare")
                        .param("sender", "user@test.edu")
                        .param("validation", "true")
                        .param("expiration", "5")
                        .param("message", "Test Message")
                        .param("recipients", "uhmfund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "jwlennon")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("John W Lennon"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "teststudent")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test21")
                        .param("authenticationRequired", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").value("Test Staff2"));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "user")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add non-UH recipient when authentication is required."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "test@google.com")
                        .param("authenticationRequired", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.cn").isEmpty());

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "help")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));

        mockMvc.perform(post("/prepare/recipient/add")
                        .param("recipient", "uhmfund")
                        .param("authenticationRequired", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.message").value("Could not add recipient due to restrictions."));
    }
}
