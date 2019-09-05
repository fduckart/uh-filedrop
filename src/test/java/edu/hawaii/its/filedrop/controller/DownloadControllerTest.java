package edu.hawaii.its.filedrop.controller;

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
    public void getDownloadNoFileDropTest() throws Exception {
        mockMvc.perform(get("/dl/randomtest"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    @WithMockUhUser
    public void getDownloadTest() throws Exception {

        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));

        mockMvc.perform(post("/prepare")
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

        FileDrop fileDrop = fileDropService.getFileDrop(1);
        fileDrop.setDownloadKey("test");
        fileDropService.saveFileDrop(fileDrop);
        assertNotNull(fileDrop);

        assertEquals("test", fileDrop.getDownloadKey());

        mockMvc.perform(get("/dl/" + fileDrop.getDownloadKey()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/download"))
                .andExpect(model().attributeExists("fileDrop"));

    }
}
