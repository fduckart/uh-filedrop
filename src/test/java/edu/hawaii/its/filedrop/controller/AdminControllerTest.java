package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.WhitelistService;
import edu.hawaii.its.filedrop.type.Whitelist;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class AdminControllerTest {

    @Value("${cas.login.url}")
    private String casLoginUrl;

    @Autowired
    private AdminController adminController;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private WhitelistService whitelistService;

    @Rule
    public GreenMailRule server = new GreenMailRule(new ServerSetup(1025, "localhost", "smtp"));

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
    public void adminFonts() throws Exception {
        mockMvc.perform(get("/admin/fonts"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fonts"));
    }

    @Test
    @WithMockUhUser
    public void adminFontsViaUh() throws Exception {
        // Access forbidden due to insufficient role.
        mockMvc.perform(get("/admin/fonts"))
                .andExpect(status().is4xxClientError())
                .andExpect(status().is(403));
    }

    @Test
    @WithAnonymousUser
    public void adminFontsViaAnonymous() throws Exception {
        // Anonymous users not allowed into admin area.
        mockMvc.perform(get("/admin/fonts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
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
        mockMvc.perform(post("/admin/lookup/")
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
        MvcResult result = mockMvc.perform(post("/admin/lookup/")
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
    @WithMockUhUser(username = "admin", roles = { "ROLE_UH", "ROLE_ADMINISTRATOR" })
    public void adminTechnology() throws Exception {
        mockMvc.perform(get("/admin/technology"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/technology"));
    }

    @Test
    @WithMockUhUser(username = "user", roles = { "ROLE_UH", "ROLE_ADMINISTRATOR" })
    public void adminGateMessage() throws Exception {
        mockMvc.perform(get("/admin/gate-message"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/gate-message"));
    }

    @Test
    @WithMockUhUser(username = "user", roles = { "ROLE_UH", "ROLE_ADMINISTRATOR" })
    public void setGateMessage() throws Exception {
        mockMvc.perform(post("/admin/gate-message")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("text", "Testing"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/gate-message"))
                .andExpect(model().attribute("message", hasProperty("id", is(1))))
                .andExpect(model().attribute("message", hasProperty("text", is("Testing"))));
    }

    @Test
    public void setGateMessageNonAdmin() throws Exception {
        mockMvc.perform(post("/admin/gate-message")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("text", "Testing"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser(username = "user", roles = { "ROLE_UH", "ROLE_ADMINISTRATOR" })
    public void setGateMessageWrongType() throws Exception {
        mockMvc.perform(post("/admin/gate-message")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "Test")
                .param("text", "Testing"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhAdmin
    public void adminIcons() throws Exception {
        mockMvc.perform(get("/admin/icons"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/icons"));
    }

    @Test
    @WithAnonymousUser
    public void adminIconsViaAnonymous() throws Exception {
        // Anonymous users not allowed into admin area.
        mockMvc.perform(get("/admin/icons"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhUser
    public void redirectWhitelist() throws Exception {
        mockMvc.perform(get("/admin/whitelist"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhAdmin
    public void getWhitelist() throws Exception {
        mockMvc.perform(get("/admin/whitelist"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/whitelist"));

        mockMvc.perform(get("/api/admin/whitelist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].entry").exists())
                .andExpect(jsonPath("$[1].entry").exists());
    }

    @Test
    @WithMockUhAdmin
    public void addWhitelist() throws Exception {
        long count0 = whitelistService.recordCount();

        mockMvc.perform(post("/api/admin/whitelist")
                .param("entry", "help")
                .param("registrant", "jwlennon"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/whitelist"));

        long count1 = whitelistService.recordCount();
        assertThat(count1, equalTo(count0 + 1));

        Whitelist whitelist = whitelistService.findWhiteList(3);
        assertEquals("help", whitelist.getEntry());
        assertEquals("ITS Help Desk", whitelist.getEntryName());
        assertEquals("jwlennon", whitelist.getRegistrant());
        assertEquals("John W Lennon", whitelist.getRegistrantName());

        mockMvc.perform(post("/api/admin/whitelist")
                .param("entry", "help")
                .param("registrant", "jwlennon"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/whitelist"));

        long count2 = whitelistService.recordCount();
        assertThat(count2, equalTo(count1 + 1));

        mockMvc.perform(get("/api/admin/whitelist"))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[3].entry").value("help"))
                .andExpect(jsonPath("$[3].registrant").value("jwlennon"));

        whitelist = whitelistService.findWhiteList(4);
        assertEquals("help", whitelist.getEntry());
        assertEquals("jwlennon", whitelist.getRegistrant());

        mockMvc.perform(post("/api/admin/whitelist")
                .param("entry", "testing")
                .param("registrant", "testing"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/whitelist"));

        long count3 = whitelistService.recordCount();
        assertThat(count3, equalTo(count2 + 1));

        mockMvc.perform(get("/api/admin/whitelist"))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[4].entry").value("testing"))
                .andExpect(jsonPath("$[4].entryName").value(""))
                .andExpect(jsonPath("$[4].registrant").value("testing"))
                .andExpect(jsonPath("$[4].registrantName").value(""));

        whitelist = whitelistService.findWhiteList(5);
        assertEquals("testing", whitelist.getEntry());
        assertEquals("", whitelist.getEntryName());
        assertEquals("testing", whitelist.getRegistrant());
        assertEquals("", whitelist.getEntryName());
    }

    @Test
    @WithMockUhAdmin
    public void deleteWhitelist() throws Exception {
        mockMvc.perform(post("/api/admin/whitelist")
                .param("entry", "help")
                .param("registrant", "jwlennon"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/whitelist"));
        mockMvc.perform(delete("/api/admin/whitelist/6"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUhAdmin
    public void email() throws Exception {
        server.start();
        mockMvc.perform(get("/admin/email"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/emails"));

        mockMvc.perform(post("/admin/email")
                .param("template", "receiver")
                .param("recipient", "admin@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/email"));

        mockMvc.perform(post("/admin/email")
                .param("template", "uploader")
                .param("recipient", "admin@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/email"));

        mockMvc.perform(post("/admin/email")
                .param("template", "whitelist")
                .param("recipient", "admin@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/email"));
        server.stop();
    }

    @Test
    @WithMockUhAdmin
    public void dashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithAnonymousUser
    public void dashboardAnonymous() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhAdmin
    public void getFileDropsTest() throws Exception {
        mockMvc.perform(get("/api/admin/filedrops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].uploader").value("test"))
                .andExpect(jsonPath("$[0].fileInfoList").isNotEmpty())
                .andExpect(jsonPath("$[0].fileInfoList[0].fileName").value("test.txt"))
                .andExpect(jsonPath("$[0].fileInfoList[0].fileType").value("text/plain"))
                .andExpect(jsonPath("$[0].fileInfoList[0].fileSize").value("1000"))
                .andExpect(jsonPath("$[0].fileInfoList[0].downloads").value("1"));
    }

    @Test
    @WithAnonymousUser
    public void getFileDropsAnonymousTest() throws Exception {
        mockMvc.perform(get("/api/admin/filedrops"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhAdmin
    public void addExpirationTest() throws Exception {
        mockMvc.perform(get("/admin/add-expiration/2/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/dashboard"));

        mockMvc.perform(get("/api/admin/filedrops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].uploader").value("test"))
                .andExpect(jsonPath("$[0].expiration").value("2021-11-16T08:30:18.023"));
    }

    @Test
    @WithAnonymousUser
    public void addExpirationAnonymousTest() throws Exception {
        mockMvc.perform(get("/admin/add-expiration/3/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhAdmin
    public void expireTest() throws Exception {
        mockMvc.perform(get("/admin/expire/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/dashboard"));

        mockMvc.perform(get("/api/admin/filedrops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].uploader").value("test"));
    }

    @Test
    @WithAnonymousUser
    public void expireAnonymousTest() throws Exception {
        mockMvc.perform(get("/admin/expire/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithAnonymousUser
    public void permissionsAnonymousTest() throws Exception {
        mockMvc.perform(get("/admin/permissions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhAdmin
    public void permissionsTest() throws Exception {
        mockMvc.perform(get("/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/permissions"));
    }
}
