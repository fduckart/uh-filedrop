package edu.hawaii.its.filedrop.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.AllowlistService;
import edu.hawaii.its.filedrop.service.ApplicationService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.type.Allowlist;
import edu.hawaii.its.filedrop.type.Faq;
import edu.hawaii.its.filedrop.type.Setting;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class AdminControllerTest {

    @RegisterExtension
    static GreenMailExtension server = new GreenMailExtension(ServerSetupTest.SMTP);

    @Value("${cas.login.url}")
    private String casLoginUrl;

    @Autowired
    private AdminController adminController;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AllowlistService allowlistService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EmailService emailService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    public void tearDown() {
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
    public void updateGateMessage() throws Exception {
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
    public void updateGateMessageNonAdmin() throws Exception {
        mockMvc.perform(post("/admin/gate-message")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("text", "Testing"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUhUser(username = "user", roles = { "ROLE_UH", "ROLE_ADMINISTRATOR" })
    public void updateGateMessageWrongType() throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/gate-message")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "Test")
                        .param("text", "Testing"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("An Error Occurred"));
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
    public void redirectAllowlist() throws Exception {
        mockMvc.perform(get("/admin/allowlist"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUhAdmin
    public void getAllowlist() throws Exception {
        mockMvc.perform(get("/admin/allowlist"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/allowlist"));

        mockMvc.perform(get("/api/admin/allowlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].entry").exists())
                .andExpect(jsonPath("$[1].entry").exists());
    }

    @Test
    @WithMockUhAdmin
    public void addAllowlist() throws Exception {
        long count0 = allowlistService.recordCount();
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("help");
        allowlist.setRegistrant("jwlennon");
        allowlist.setExpired(false);

        String jsonRequest = objectToJSON(allowlist);

        mockMvc.perform(post("/api/admin/allowlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entry").value("help"))
                .andExpect(jsonPath("$.registrant").value("jwlennon"));

        long count1 = allowlistService.recordCount();
        assertThat(count1, equalTo(count0 + 1));

        allowlist = allowlistService.findById(3);
        assertEquals("help", allowlist.getEntry());
        assertEquals("jwlennon", allowlist.getRegistrant());

        allowlist = new Allowlist();
        allowlist.setExpired(false);
        allowlist.setEntry("help");
        allowlist.setRegistrant("jwlennon");
        jsonRequest = objectToJSON(allowlist);

        mockMvc.perform(post("/api/admin/allowlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entry").value("help"))
                .andExpect(jsonPath("$.registrant").value("jwlennon"));

        long count2 = allowlistService.recordCount();
        assertThat(count2, equalTo(count1 + 1));

        mockMvc.perform(get("/api/admin/allowlist"))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[3].entry").value("help"))
                .andExpect(jsonPath("$[3].registrant").value("jwlennon"));

        allowlist = allowlistService.findById(4);
        assertEquals("help", allowlist.getEntry());
        assertEquals("jwlennon", allowlist.getRegistrant());

        allowlist = new Allowlist();
        allowlist.setExpired(false);
        allowlist.setEntry("testing");
        allowlist.setRegistrant("testing");
        jsonRequest = objectToJSON(allowlist);

        mockMvc.perform(post("/api/admin/allowlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        long count3 = allowlistService.recordCount();
        assertThat(count3, equalTo(count2 + 1));

        mockMvc.perform(get("/api/admin/allowlist"))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[4].entry").value("testing"))
                .andExpect(jsonPath("$[4].registrant").value("testing"));

        allowlist = allowlistService.findById(5);
        assertEquals("testing", allowlist.getEntry());
        assertEquals("testing", allowlist.getRegistrant());
    }

    @Test
    @WithMockUhAdmin
    public void deleteAllowlist() throws Exception {
        Allowlist allowlist = new Allowlist();
        allowlist.setExpired(false);
        allowlist.setEntry("help");
        allowlist.setRegistrant("jwlennon");
        String jsonRequest = objectToJSON(allowlist);
        MvcResult result = mockMvc.perform(post("/api/admin/allowlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
        Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(delete("/api/admin/allowlist/" + id))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUhAdmin
    public void email() throws Exception {
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
                        .param("template", "allowlist")
                        .param("recipient", "admin@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/email"));
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
                .andExpect(jsonPath("$[0].expiration").value("2051-11-16T08:30:18.023"));
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
    public void settingsAnonymousTest() throws Exception {
        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));

        mockMvc.perform(post("/admin/settings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(casLoginUrl + "**"));
    }

    @Test
    @WithMockUhAdmin
    public void settingsTest() throws Exception {
        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/settings"))
                .andExpect(model().attributeExists("settings"));

        mockMvc.perform(post("/admin/settings/1")
                        .param("value", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/settings"))
                .andExpect(flash().attributeExists("alert"));

        Setting setting = applicationService.findSetting(1);
        assertThat(setting.getValue(), equalTo("false"));

        mockMvc.perform(post("/admin/settings/1")
                        .param("value", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/settings"))
                .andExpect(flash().attributeExists("alert"));

        mockMvc.perform(post("/admin/settings")
                        .param("key", "test")
                        .param("value", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/settings"))
                .andExpect(flash().attributeExists("alert"));
    }

    @Test
    @WithMockUhAdmin
    public void faqTest() throws Exception {
        Faq faq = new Faq();
        faq.setQuestion("test question");
        faq.setAnswer("test answer");

        mockMvc.perform(get("/admin/faq"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/faq"));

        mockMvc.perform(post("/api/admin/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJSON(faq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("test question"))
                .andExpect(jsonPath("$.answer").value("test answer"));

        faq = applicationService.findFaq(applicationService.findFaqs().size() - 1);

        mockMvc.perform(post("/api/admin/faq/" + faq.getId())
                        .param("question", "new question")
                        .param("answer", "test answer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("new question"))
                .andExpect(jsonPath("$.answer").value("test answer"));

        mockMvc.perform(delete("/api/admin/faq/" + faq.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("new question"))
                .andExpect(jsonPath("$.answer").value("test answer"));
    }

    public String objectToJSON(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(object);
    }
}
