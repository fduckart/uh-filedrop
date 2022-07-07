package edu.hawaii.its.filedrop.access;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Role.SecurityRole;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UserBuilderTest {

    @Autowired
    private UserBuilder userBuilder;

    @Test
    public void testAdminUsers() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "duckart");
        map.put("uhuuid", "89999999");
        User user = userBuilder.make(map);

        // Basics.
        assertEquals("duckart", user.getUsername());
        assertEquals("duckart", user.getUid());
        assertEquals("89999999", user.getUhuuid());

        // Granted Authorities.
        assertTrue(user.getAuthorities().size() > 0);
        assertFalse(user.hasRole(SecurityRole.ANONYMOUS));
        assertTrue(user.hasRole(SecurityRole.UH));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR));

        map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhuuid", "10000001");
        user = userBuilder.make(map);

        assertEquals("someuser", user.getUsername());
        assertEquals("someuser", user.getUid());
        assertEquals("10000001", user.getUhuuid());

        assertTrue(user.getAuthorities().size() > 0);
        assertFalse(user.hasRole(SecurityRole.ANONYMOUS));
        assertTrue(user.hasRole(SecurityRole.UH));
        assertFalse(user.hasRole(SecurityRole.NON_UH));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR));

        // Granted Authorities, string version.
        assertFalse(user.hasRole((String) null));
        assertFalse(user.hasRole(""));

        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.longName()));
        assertTrue(user.hasRole(SecurityRole.UH.longName()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.longName()));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR.longName()));

        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.name()));
        assertTrue(user.hasRole(SecurityRole.UH.name()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.name()));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR.name()));
    }

    @Test
    public void testCoordinators() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "jjcale");
        map.put("uhuuid", "10000004");
        User user = userBuilder.make(map);

        // Basics.
        assertEquals("jjcale", user.getUsername());
        assertEquals("jjcale", user.getUid());
        assertEquals("10000004", user.getUhuuid());

        // Granted Authorities.
        assertEquals(2, user.getAuthorities().size());
        assertFalse(user.hasRole(SecurityRole.ANONYMOUS));
        assertTrue(user.hasRole(SecurityRole.UH));
        assertFalse(user.hasRole(SecurityRole.NON_UH));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR));

        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.name()));
        assertTrue(user.hasRole(SecurityRole.UH.name()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.name()));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR.name()));

        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.longName()));
        assertTrue(user.hasRole(SecurityRole.UH.longName()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.longName()));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR.longName()));
    }

    @Test
    public void testCoordinatorsWithMultivalueUid() {
        Map<String, Object> map = new HashMap<>();
        ArrayList<Object> uids = new ArrayList<>();
        uids.add("aaaaaaa");
        uids.add("bbbbbbb");
        map.put("uid", uids);
        map.put("uhuuid", "10000003");
        User user = userBuilder.make(map);

        // Basics.
        assertEquals("aaaaaaa", user.getUsername());
        assertEquals("aaaaaaa", user.getUid());
        assertEquals("10000003", user.getUhuuid());

        // Granted Authorities.
        assertEquals(2, user.getAuthorities().size());
        assertFalse(user.hasRole(SecurityRole.ANONYMOUS));
        assertFalse(user.hasRole(SecurityRole.NON_UH));
        assertTrue(user.hasRole(SecurityRole.UH));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR));

        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.name()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.name()));
        assertTrue(user.hasRole(SecurityRole.UH.name()));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR.name()));

        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.longName()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.longName()));
        assertTrue(user.hasRole(SecurityRole.UH.longName()));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR.longName()));
    }

    @Test
    public void testNotAnCoordinator() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "nobody");
        map.put("uhuuid", "10000009");
        User user = userBuilder.make(map);

        // Basics.
        assertEquals("nobody", user.getUsername());
        assertEquals("nobody", user.getUid());
        assertEquals("10000009", user.getUhuuid());

        // Granted Authorities.
        assertEquals(1, user.getAuthorities().size());
        assertFalse(user.hasRole(SecurityRole.ANONYMOUS));
        assertFalse(user.hasRole(SecurityRole.NON_UH));
        assertTrue(user.hasRole(SecurityRole.UH));
        assertFalse(user.hasRole(SecurityRole.ADMINISTRATOR));

        // Granted Authorities, string version.
        assertFalse(user.hasRole(SecurityRole.ANONYMOUS.longName()));
        assertFalse(user.hasRole(SecurityRole.NON_UH.longName()));
        assertTrue(user.hasRole(SecurityRole.UH.longName()));
        assertFalse(user.hasRole(SecurityRole.ADMINISTRATOR.longName()));
    }

    @Test
    public void testUidNull() {
        List<String> uids = new ArrayList<>();
        uids.add(null);
        Map<String, List<String>> map = new HashMap<>();
        map.put("uid", uids);

        try {
            userBuilder.make(map);
            fail("Should not reach here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), UsernameNotFoundException.class);
            assertThat(e.getMessage(), containsString("uid is blank"));
        }
    }

    @Test
    public void testUidBlank() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "  ");

        try {
            userBuilder.make(map);
            fail("Should not reach here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), UsernameNotFoundException.class);
            assertThat(e.getMessage(), containsString("uid is blank"));
        }
    }

    @Test
    public void testUidEmpty() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "");

        try {
            userBuilder.make(map);
            fail("Should not reach here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), UsernameNotFoundException.class);
            assertThat(e.getMessage(), containsString("uid is blank"));
        }
    }

    @Test(expected = UsernameNotFoundException.class)
    public void make() {
        userBuilder.make(new HashMap<>());
    }
}
