package edu.hawaii.its.filedrop.controller;

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
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
public class DownloadControllerTest {

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
    }

    @Test
    @WithMockUhUser
    public void downloadTest() throws Exception {

        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "test@test.com", "test2@test.com")
                .param("message", "test")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/prepare/files")));

        FileDrop fileDrop = fileDropService.findFileDrop(3);

        mockMvc.perform(get("/prepare/files/" + fileDrop.getUploadKey()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "test data".getBytes());

        fileDrop.setDownloadKey("test");
        fileDropService.saveFileDrop(fileDrop);
        assertNotNull(fileDrop);

        mockMvc.perform(multipart("/prepare/files/" + fileDrop.getUploadKey())
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        assertEquals("test", fileDrop.getDownloadKey());

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/sl/" + fileDrop.getDownloadKey()));

        mockMvc.perform(get("/sl/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download"))
                .andExpect(model().attributeExists("fileDrop"));

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey() + "/" + mockMultipartFile.getOriginalFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk());

    }

    @Test
    @WithAnonymousUser
    public void downloadNoAuth() throws Exception {
        mockMvc.perform(get("/dl/downloadKey2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("fileDrop"));
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
    }
}
