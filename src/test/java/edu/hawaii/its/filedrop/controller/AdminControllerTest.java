package edu.hawaii.its.filedrop.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AdminControllerTest {

    @Value("${cas.login.url}")
    private String casLoginUrl;

    @Autowired
    private AdminController adminController;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void construction() {
        assertNotNull(adminController);
    }

    @Test
    @WithMockUhUser(username = "admin", roles = { "ROLE_UH", "ROLE_ADMINISTRATOR" })
    public void admin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin"));
    }

    @Test
    @WithAnonymousUser
    public void adminViaAnonymous() throws Exception {
        // Anonymous users not allowed into admin area.
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhUser
    public void adminViaUh() throws Exception {
        // Access forbidden due to insufficient role.
        mockMvc.perform(get("/admin"))
                .andExpect(status().is4xxClientError())
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUhAdmin
    public void adminUserRole() throws Exception {
        mockMvc.perform(get("/admin/application/role"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/application-role"));
    }

    @Test
    @WithMockUhAdmin
    public void adminLookup() throws Exception {
        mockMvc.perform(get("/admin/lookup"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/lookup"));
    }

    @Test
    @WithAnonymousUser
    public void adminLookupLdapViaAnonymous() throws Exception {
        mockMvc.perform(post("/admin/lookup/ldap").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser
    public void adminLookupLdapViaUh() throws Exception {
        // Access forbidden due to insufficient role.
        mockMvc.perform(post("/admin/lookup/ldap").with(csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUhAdmin(username = "beno")
    public void adminLookupLdapViaAdmin() throws Exception {
        mockMvc.perform(post("/admin/lookup/ldap")
                .param("search", "rthompson"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/lookup"))
                .andExpect(model().attributeExists("person"))
                .andExpect(model().attribute("person",
                        hasProperty("givenName", equalTo("Richard"))))
                .andExpect(model().attribute("person",
                        hasProperty("sn", equalTo("Thompson"))));
    }

    @Test
    @WithMockUhAdmin(username = "duckart")
    public void adminLookupLdapViaAdminAgain() throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/lookup/ldap")
                .param("search", "rthompson")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/lookup"))
                .andExpect(model().attributeExists("person"))
                .andExpect(model().attribute("person",
                        hasProperty("givenName", equalTo("Richard"))))
                .andExpect(model().attribute("person",
                        hasProperty("sn", equalTo("Thompson"))))
                .andReturn();

        MockHttpServletResponse mockResponse = result.getResponse();
        assertThat(mockResponse.getContentType(), equalTo("text/html;charset=UTF-8"));

        Collection<String> responseHeaders = mockResponse.getHeaderNames();
        assertNotNull(responseHeaders);
        assertThat(responseHeaders.size(), equalTo(8));

        List<String> headers = new ArrayList<>(responseHeaders);
        Collections.sort(headers);

        assertThat(headers.get(0), equalTo("Cache-Control"));
        assertThat(headers.get(1), equalTo("Content-Language"));
        assertThat(headers.get(2), equalTo("Content-Type"));
        assertThat(headers.get(3), equalTo("Expires"));
        assertThat(headers.get(4), equalTo("Pragma"));
        assertThat(headers.get(5), equalTo("X-Content-Type-Options"));
        assertThat(headers.get(6), equalTo("X-Frame-Options"));
        assertThat(headers.get(7), equalTo("X-XSS-Protection"));
    }

    @Test
    @WithMockUhUser(username = "admin", roles = {"ROLE_UH", "ROLE_ADMINISTRATOR"})
    public void adminTechnology() throws Exception {
        mockMvc.perform(get("/admin/technology"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/technology"));
    }

    @Test
    @WithMockUhUser(username = "user", roles = {"ROLE_UH", "ROLE_ADMINISTRATOR"})
    public void adminGateMessage() throws Exception {
        mockMvc.perform(get("/admin/gate-message"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/gate-message"));
    }

    @Test
    @WithMockUhUser(username = "user", roles = {"ROLE_UH", "ROLE_ADMINISTRATOR"})
    public void setGateMessage() throws Exception {
        mockMvc.perform(put("/admin/gate-message")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("text", "Testing"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/gate-message"))
            .andExpect(model().attribute("message", hasProperty("id", is(1))))
            .andExpect(model().attribute("message", hasProperty("text", is("Testing"))));
    }

}
