package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
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

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.FileDropRepository;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.FileSystemStorageService;
import edu.hawaii.its.filedrop.type.FileData;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileDropInfo;
import edu.hawaii.its.filedrop.type.FileSet;

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
    private FileSystemStorageService fileSystemStorageService;

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

        MvcResult mvcResult = mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/4")
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getHeaderValue("Content-Disposition"), equalTo("attachment; filename=\"test.txt\""));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/zip"))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/complete/" + fileDrop.getUploadKey()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/dl/" + fileDrop.getDownloadKey()));

        mvcResult = mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getHeaderValue("Content-Disposition"),
            equalTo("attachment; filename=\"FileDrop(" + fileDrop.getDownloadKey() + ").zip\""));

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
        mockMvc.perform(get("/dl/downloadKey3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/sl/downloadKey3"));

        mockMvc.perform(get("/sl/downloadKey3"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download-error"))
                .andExpect(model().attribute("error", "Download not found"));

        mockMvc.perform(get("/dl/downloadKey3/999"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/dl/downloadKey3/zip"))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/dl/notarealkey/zip"))
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
                .andExpect(model().attribute("error", "Download not found"));
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

    @Test
    @WithMockUhUser
    public void fileDataDownloadTest() throws Exception {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setValid(true);
        fileDrop.setExpiration(LocalDateTime.now().minusMinutes(1));
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setDownloadKey("testdlkey");
        fileDrop.setUploadKey("testulkey");
        fileDrop.setEncryptionKey("testenckey");
        fileDrop.setUploader("test2");
        fileDrop.setUploaderFullName("Test 2");
        fileDrop = fileDropService.saveFileDrop(fileDrop);
        fileDropService.addRecipients(fileDrop, "user");


        MockMultipartFile mockMultipartFile =
            new MockMultipartFile("file", "test.tst", "test/type", "test data".getBytes());

        FileSet fileSet = new FileSet();
        fileSet.setSize(mockMultipartFile.getSize());
        fileSet.setType(mockMultipartFile.getContentType());
        fileSet.setFileName(mockMultipartFile.getOriginalFilename());
        fileSet.setComment("test comment");
        fileSet.setFileDrop(fileDrop);
        fileSet = fileDropService.saveFileSet(fileSet);

        fileDrop = fileDropService.findFileDrop(fileDrop.getId());

        assertNotNull(fileDrop);
        assertNotNull(fileDrop.getFileSet());

        fileSystemStorageService.storeFileSet(mockMultipartFile.getResource(),
            Paths.get(fileDrop.getDownloadKey(), fileSet.getId().toString()));
        Resource resource = fileSystemStorageService.loadAsResource(
            Paths.get(fileDrop.getDownloadKey(), fileSet.getId().toString()).toString());
        resource.getFile().deleteOnExit();

        FileDropInfo fileDropInfo = fileDropService.getFileDropInfo(fileDrop);

        assertNotNull(fileDropInfo);
        assertNotNull(fileDropInfo.getFileInfoList().get(0));
        assertEquals(fileDropInfo.getFileInfoList().get(0).getFileName(), "test.tst");

        MvcResult mvcResult = mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/" + fileSet.getId()))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getHeaderValue("Content-Disposition"), equalTo("attachment; filename=\"test.tst\""));

        FileData fileData = new FileData();
        fileData.setFileName("test data.tst");
        fileData.setComment("test comment");
        fileData.setFileSet(fileSet);
        fileData = fileDropService.saveFileData(fileData);

        fileDropInfo = fileDropService.getFileDropInfo(fileDrop);
        assertNotNull(fileDropInfo);
        assertNotNull(fileDropInfo.getFileInfoList().get(0));
        assertEquals(fileDropInfo.getFileInfoList().get(0).getFileName(), "test data.tst");

        assertEquals(fileSet.getId(), fileData.getFileSet().getId());

        mvcResult = mockMvc.perform(get("/dl/testdlkey/" + fileSet.getId()))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getHeaderValue("Content-Disposition"), equalTo("attachment; filename=\"test data.tst\""));

    }

}
