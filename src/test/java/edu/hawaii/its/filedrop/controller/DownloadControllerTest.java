package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class DownloadControllerTest {

    @Autowired
    private DownloadController downloadController;

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void construct() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUhUser
    public void downloadNoFileDropTest() throws Exception {
        mockMvc.perform(get("/dl/randomtest"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download-error"))
                .andExpect(model().attribute("error", "Download not found"));

        mockMvc.perform(get("/sl/randomtest"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download-error"))
                .andExpect(model().attribute("error", "Download not found"));
    }

    @Test
    @WithMockUhUser(username = "jmess")
    public void downloadTest() throws Exception {

        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "jwlennon")
                .param("message", "test")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        Integer count = Long.valueOf(fileDropRepository.count()).intValue();
        FileDrop fileDrop = fileDropService.findFileDrop(count);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/prepare/files/" + fileDrop.getUploadKey())
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/sl/" + fileDrop.getDownloadKey()));

        mockMvc.perform(get("/sl/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download"))
                .andExpect(model().attributeExists("fileDrop"));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/3")
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/9999"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithAnonymousUser
    public void downloadNoAuth() throws Exception {
        mockMvc.perform(get("/dl/downloadKey2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("fileDrop"));

        mockMvc.perform(get("/dl/downloadKey/99999"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhUser
    public void downloadUnauthorized() throws Exception {
        mockMvc.perform(get("/dl/downloadKey"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/sl/downloadKey"));

        mockMvc.perform(get("/sl/downloadKey"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download-error"))
                .andExpect(model().attribute("error", "You are not a recipient for this drop."));

        mockMvc.perform(get("/dl/downloadKey/999"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhUser
    public void downloadNullFileDrop() throws Exception {
        mockMvc.perform(get("/dl/123/9999"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhUser
    public void expireErrorTest() throws Exception {
        mockMvc.perform(get("/expire/test123"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download-error"))
                .andExpect(model().attribute("error", "Download not found"));

        mockMvc.perform(get("/expire/downloadKey3"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download-error"))
                .andExpect(model().attribute("error", "You are not authorized to expire this drop"));
    }

    @Test
    @WithMockUhUser(username = "kcobain")
    public void expireTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "user")
                .param("message", "test")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        Integer count = Long.valueOf(fileDropRepository.count()).intValue();
        FileDrop fileDrop = fileDropService.findFileDrop(count);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/prepare/files/" + fileDrop.getUploadKey())
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/expire/" + fileDrop.getDownloadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/expired"))
                .andExpect(model().attributeExists("expiration"));

        mockMvc.perform(get("/sl/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/expired"))
                .andExpect(model().attributeExists("expiration"));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/1")
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhUser(username = "dgrohl")
    public void expireNoAuthTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "user")
                .param("message", "test")
                .param("validation", "false")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        Integer count = Long.valueOf(fileDropRepository.count()).intValue();
        FileDrop fileDrop = fileDropService.findFileDrop(count);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/prepare/files/" + fileDrop.getUploadKey())
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/expire/" + fileDrop.getDownloadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/expired"))
                .andExpect(model().attributeExists("expiration"));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/1")
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhUser(username = "dgrohl")
    public void isDownloadAllowed() {
        String username = "dgrohl";

        assertThat(downloadController.currentUser().getUsername(), equalTo(username));
        assertThat(downloadController.currentUser().getUid(), equalTo(username));

        FileDrop fileDrop = fileDropService.findFileDrop(3);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(false));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards@example.com"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(2);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(false));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(4);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDrop.getRecipients().size(), greaterThanOrEqualTo(2));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards@example.com"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon@hawaii.edu"), equalTo(true));
    }

    @Test
    @WithMockUhUser(username = "jwlennon")
    public void isDownloadAllowedTwo() {
        String username = "jwlennon";
        assertThat(downloadController.currentUser().getUsername(), equalTo(username));
        assertThat(downloadController.currentUser().getUid(), equalTo(username));

        FileDrop fileDrop = fileDropService.findFileDrop(3);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(false));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(2);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(false));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(4);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon@hawaii.edu"), equalTo(true));
    }

    @Test
    @WithMockUhUser(username = "test22")
    public void isDownloadAllowedThree() {
        String username = "test22";
        assertThat(downloadController.currentUser().getUsername(), equalTo(username));
        assertThat(downloadController.currentUser().getUid(), equalTo(username));

        FileDrop fileDrop = fileDropService.findFileDrop(3);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(false));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(2);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(1);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(true));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(false));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(true));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));

        fileDrop = fileDropService.findFileDrop(4);
        assertThat(fileDrop.isAuthenticationRequired(), equalTo(false));
        assertThat(downloadController.isDownloadAllowed(fileDrop), equalTo(true));

        assertThat(fileDropService.isAuthorized(fileDrop, username), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "recipient"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "krichards"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "test"), equalTo(false));
        assertThat(fileDropService.containsRecipient(fileDrop, "jwlennon"), equalTo(false));
    }

}
