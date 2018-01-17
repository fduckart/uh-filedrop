package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RoleRestControllerTest {

    final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON.getType(),
                    MediaType.APPLICATION_JSON.getSubtype(),
                    Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
    }

    @Test
    public void httpGetRoles() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(13)))
                .andExpect(jsonPath("$[0].role").value("APPLICANT"))
                .andExpect(jsonPath("$[1].role").value("COORDINATOR"))
                .andExpect(jsonPath("$[2].role").value("DPC_REVIEWER"))
                .andExpect(jsonPath("$[3].role").value("DPC_CHAIR"))
                .andExpect(jsonPath("$[4].role").value("DC_REVIEWER"))
                .andExpect(jsonPath("$[5].role").value("DC_CHAIR"))
                .andExpect(jsonPath("$[6].role").value("DEAN_REVIEWER"))
                .andExpect(jsonPath("$[7].role").value("DEAN_CHAIR"))
                .andExpect(jsonPath("$[8].role").value("TPRC_REVIEWER"))
                .andExpect(jsonPath("$[9].role").value("TPRC_CHAIR"))
                .andExpect(jsonPath("$[10].role").value("EXCLUDED"))
                .andExpect(jsonPath("$[11].role").value("ADMINISTRATOR"))
                .andExpect(jsonPath("$[12].role").value("SUPER_USER"));
    }

}
