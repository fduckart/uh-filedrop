package edu.hawaii.its.filedrop.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.SpaceCheckService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class HomeControllerTest {

    @Value("${url.home}")
    private String homeUrl;

    @Value("${cas.login.url}")
    private String casLoginUrl;

    @Value("${cas.logout.url}")
    private String casLoginOut;

    @Autowired
    private HomeController homeController;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Before
    public void setUp() {
        // Make sure the email service does not send emails.
        homeController.getEmailService().setEnabled(false);
        assertFalse(homeController.getEmailService().isEnabled());

        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void testConstruction() {
        assertNotNull(homeController);
    }

    @Test
    public void requestHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("gatemessage", equalTo("Welcome to the University of Hawai'i FileDrop application.")));
    }

    @Test
    public void requestHomeSpaceFull() throws Exception {
        spaceCheckService.setBytesFree(0L);
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("gatemessage", equalTo("FileDrop is currently unavailable.")));
        spaceCheckService.update();
    }

    @Test
    public void requestCampus() throws Exception {
        mockMvc.perform(get("/campus"))
                .andExpect(status().isOk())
                .andExpect(view().name("campus"));

        mockMvc.perform(get("/campuses"))
                .andExpect(status().isOk())
                .andExpect(view().name("campus"));
    }

    @Test
    public void requestContact() throws Exception {
        mockMvc.perform(get("/help/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("help/contact"));
    }

    @Test
    public void requestFaq() throws Exception {
        mockMvc.perform(get("/help/faq"))
                .andExpect(status().isOk())
                .andExpect(view().name("help/faq"));
    }

    @Test
    @WithAnonymousUser
    public void loginViaAnonymous() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhUser
    public void loginViaUh() throws Exception {
        // Logged in already, URL redirects back to home page.
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/user"));
    }

    @Test
    @WithMockUhUser
    public void logoutViaUh() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(mvcResult.getResponse().getRedirectedUrl(), equalTo(homeUrl));
    }

    @Test
    @WithAnonymousUser
    public void prepareViaAnonymous() throws Exception {
        mockMvc.perform(get("/prepare"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhUser
    public void prepareViaUh() throws Exception {
        // Logged in already, URL redirects back to home page.
        mockMvc.perform(get("/prepare"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/prepare"));
    }

    @Test
    @WithAnonymousUser
    public void userViaAnonymous() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhUser
    public void userViaUh() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user"));
    }

    @Test
    @WithAnonymousUser
    public void userData() throws Exception {
        // Anonymous users not allowed here.
        mockMvc.perform(post("/user/data").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser(username = "beno")
    public void userDataViaUhUser() throws Exception {
        // Anonymous users not allowed here.
        mockMvc.perform(post("/user/data").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/user"));
    }

    @Test
    public void fonts() throws Exception {
        mockMvc.perform(get("/help/fonts"))
                .andExpect(status().isOk())
                .andExpect(view().name("help/fonts"));
    }

    @Test
    public void permissions() throws Exception {
        mockMvc.perform(get("/help/permissions"))
                .andExpect(status().isOk())
                .andExpect(view().name("help/permissions"));
    }

    @Test
    public void requestUrl404() throws Exception {
        mockMvc.perform(get("/404"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void requestNonExistentUrl() throws Exception {
        mockMvc.perform(get("/not-a-url"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithAnonymousUser
    public void adminUser() throws Exception {
        // Anonymous users not allowed into admin area.
        mockMvc.perform(get("/user"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void exceptionTest() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setControllerAdvice(new ErrorControllerAdvice())
                .build();
    }

}
