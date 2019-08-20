package edu.hawaii.its.filedrop.controller;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class PrepareControllerTest {

    @Autowired
    private PrepareController prepareController;

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
                .andExpect(view().name("user/prepare"))
                .andReturn();

        mockMvc.perform(post("/prepare")
                .param("recipients", "test", "test2"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Test
    @WithMockUhUser
    public void taskRedirectTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"))
                .andReturn();

        mockMvc.perform(get("/prepare/files"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Test
    @WithMockUhUser
    public void addFilesTest() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"))
                .andReturn();

        mockMvc.perform(post("/prepare")
                .param("recipients", "test", "test2"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        mockMvc.perform(get("/prepare/files"))
                .andExpect(model().attribute("recipients", equalTo(Arrays.array("test", "test2"))))
                .andReturn();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("user-file", "test.txt",
                "text/plain", "test data".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/prepare/files")
                .file("file", mockMultipartFile.getBytes())
                .param("comment", "test comment")
                .characterEncoding("UTF-8"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }
}
