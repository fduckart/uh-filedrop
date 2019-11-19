package edu.hawaii.its.filedrop.controller;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class PrepareControllerTest {

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
    public void addRecipientsTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "test", "test2")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare/files"));


        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("test", "test2")));
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
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare/files"));

        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("test", "John W Lennon")));
    }

    @Test
    @WithMockUhUser(username = "jwlennon", name = "John W Lennon")
    public void addNoRecipients() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "jwlennon@hawaii.edu")
                .param("recipients", "")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare/files"));

        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/files"))
                .andExpect(model().attribute("recipients", contains("John W Lennon")));
    }

    @Test
    @WithMockUhUser
    public void taskRedirectTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare"));
    }

    @Test
    @WithMockUhUser
    public void addFilesTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("sender", "test")
                .param("recipients", "test", "test2")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/prepare/files"));

        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("recipients"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "test data".getBytes());

        mockMvc.perform(multipart("/prepare/files")
                .file(mockMultipartFile)
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        FileDrop fileDrop = fileDropService.findFileDrop(2);
        assertNotNull(fileDrop);

        List<FileSet> fileSets = fileDropService.findFileSets(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());
        assertEquals("test.txt", fileSets.get(0).getFileName());
        assertEquals("text/plain", fileSets.get(0).getType());
        assertEquals("test comment", fileSets.get(0).getComment());
    }
}
