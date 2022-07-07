package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.ValidationRepository;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.type.Validation;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ValidationControllerTest {

    @Rule
    public GreenMailRule server = new GreenMailRule(ServerSetupTest.SMTP);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private EmailService emailService;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        server.start();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void validateTest() throws Exception {
        server.start();
        mockMvc.perform(post("/validate")
                        .param("name", "Jon Mess")
                        .param("value", "jmess@test.com")
                        .param("email", ""))
                .andExpect(view().name("validation/validation-sent"))
                .andExpect(model().attribute("email", "jmess@test.com"))
                .andReturn();
        server.stop();

        Validation validation = validationRepository.findAll().get(0);
        validation.setValidationKey("validationKey");
        validation.setIpAddress("0.0.0.0");
        assertThat(validation.getValidationKey(), equalTo("validationKey"));
        assertThat(validation.getName(), equalTo("Jon Mess"));
        assertThat(validation.getAddress(), equalTo("jmess@test.com"));
        assertThat(validation.getIpAddress(), equalTo("0.0.0.0"));
        assertTrue(validation.getCreated().isBefore(LocalDateTime.now()));
    }

    @Test
    public void validateSpamTest() throws Exception {
        mockMvc.perform(post("/validate")
                        .param("name", "Jon Mess")
                        .param("value", "jmess@test.com")
                        .param("email", "spam@test.com"))
                .andExpect(view().name("validation/validation-sent"))
                .andExpect(model().attribute("email", "spam@test.com"))
                .andReturn();
    }

}
