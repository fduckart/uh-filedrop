package edu.hawaii.its.filedrop.controller;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
                .param("recipients", "test", "test2")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection());

        FileDrop fileDrop = fileDropService.getFileDrop(1);
        assertNotNull(fileDrop);
        assertTrue(fileDrop.isAuthenticationRequired());
        assertTrue(fileDrop.isValid());
        assertEquals(2019, fileDrop.getCreated().getYear());
        assertEquals(2019, fileDrop.getExpiration().getYear());
        assertEquals("user", fileDrop.getUploader());
        assertEquals("User", fileDrop.getUploaderFullName());
    }

    @Test
    @WithMockUhUser
    public void addFiles() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("recipients", "test", "test2")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser
    public void taskRedirectTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser
    public void addFilesTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
                .param("recipients", "test", "test2")
                .param("validation", "true")
                .param("expiration", "5"))
                .andExpect(status().is3xxRedirection());

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

        FileDrop fileDrop = fileDropService.getFileDrop(1);
        assertNotNull(fileDrop);

        List<FileSet> fileSets = fileDropService.getFileSets(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());
        assertEquals("test.txt", fileSets.get(0).getFileName());
        assertEquals("text/plain", fileSets.get(0).getType());
        assertEquals("test comment", fileSets.get(0).getComment());
    }
}
