package edu.hawaii.its.filedrop.access;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class UhCasAttributesTest {

    @Test
    public void loadNullMap() {
        UhCasAttributes attributes = new UhCasAttributes(null);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUhUuid());
        assertEquals("", attributes.getUid());

        assertEquals("", attributes.getValue("not-a-key"));
    }

    @Test
    public void loadMapValid() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uhuuid", "666666");
        map.put("uid", "duckart");
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("666666", attributes.getUhUuid());
        assertEquals("duckart", attributes.getUid());
        assertEquals("", attributes.getValue("not-a-key"));
        assertEquals("", attributes.getValue(null));
    }

    @Test
    public void loadMapInvalidValueType() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uhuuid", "666666");
        map.put("uid", new Integer(666));
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("666666", attributes.getUhUuid());
        assertEquals("", attributes.getUid()); // Internal error.
        assertEquals("", attributes.getValue("not-a-key"));
    }

    @Test
    public void loadMapInvalidKeyType() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uhuuid", "666666");
        map.put(new Integer(666), new Integer(666));
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("666666", attributes.getUhUuid());
        assertEquals("", attributes.getUid());
        assertEquals("", attributes.getValue("not-a-key"));
    }

    @Test
    public void loadMapInvalidTypes() {
        Map<Object, Object> map = new HashMap<>();
        map.put(new Integer(666), new Integer(666));
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUhUuid());
        assertEquals("", attributes.getUid());
        assertEquals("", attributes.getValue("not-a-key"));
    }

    @Test
    public void loadMapWithArrayList() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uHuuid", "17958670");
        List<Object> uids = new ArrayList<Object>();
        uids.add("fduckart");
        uids.add("mjrules");
        map.put("uid", uids);
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("17958670", attributes.getUhUuid());
        assertEquals("17958670", attributes.getUhUuid());
        assertEquals("fduckart", attributes.getUid());
    }

    @Test
    public void loadMapWithArrayListWithNullEntries() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uHuuid", "17958670");
        List<Object> uids = new ArrayList<Object>();
        uids.add(null);
        uids.add(null);
        map.put("uid", uids);
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUid());
        assertEquals("17958670", attributes.getUhUuid());
    }

    @Test
    public void loadMapWithArrayListWithEmptyEntries() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uHuuid", "17958670");
        List<Object> uids = new ArrayList<Object>();
        uids.add("");
        uids.add("");
        map.put("uid", uids);
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUid());
        assertEquals("17958670", attributes.getUhUuid());
    }

    @Test
    public void loadMapWithArrayListWithManyEntries() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uHuuid", "17958670");
        List<Object> uids = new ArrayList<Object>();
        for (int i = 0; i < 50; i++) {
            uids.add("");
        }
        uids.add("fduckart");
        map.put("uid", uids);
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUid()); // Note this result.
        assertEquals("17958670", attributes.getUhUuid());
    }

    @Test
    public void loadMapWithNullMap() {
        Map<Object, Object> map = null;
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUhUuid());
        assertEquals("", attributes.getUid());
    }

    @Test
    public void loadMapWithNullMapEntry() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uid", null);
        map.put("uHuuid", null);
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUhUuid());
        assertEquals("", attributes.getUid());
    }

    @Test
    public void loadMapWithEmptyMapEntry() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uid", new ArrayList<Object>());
        map.put("uhuuid", new ArrayList<Object>(0));
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("", attributes.getUhUuid());
        assertEquals("", attributes.getUid());
    }

    @Test
    public void loadMapWithNullKey() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uhuuid", "17958670");
        map.put(null, "fduckart");
        UhCasAttributes attributes = new UhCasAttributes(map);
        assertEquals("", attributes.getUsername());
        assertEquals("17958670", attributes.getUhUuid());
        assertEquals("", attributes.getUid()); // Note this result.
    }

    @Test
    public void loadMapWithUnexpectedType() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uhuuid", "666666");

        Map<Long, java.util.Date> uidMap = new HashMap<Long, java.util.Date>();
        uidMap.put(new Long(666), new java.util.Date());
        map.put("uid", uidMap);

        UhCasAttributes attributes = new UhCasAttributes(map);

        assertEquals("", attributes.getUsername());
        assertEquals("666666", attributes.getUhUuid());
        assertEquals("", attributes.getUid()); // Note result.
    }

    @Test
    public void loadMapWithNullUsername() {
        String username = null;
        Map<Object, Object> map = new HashMap<>();
        map.put("uid", "duckart");
        map.put("uhuuid", "6666666");
        UhCasAttributes attributes = new UhCasAttributes(username, map);
        assertEquals("duckart", attributes.getUid());
        assertEquals("6666666", attributes.getUhUuid());
        assertEquals("", attributes.getUsername());
    }

    @Test
    public void misc() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uid", "duckart");
        map.put("uhuuid", "666666");
        map.put("cn", "Frank");
        map.put("mail", "frank@example.com");
        map.put("eduPersonAffiliation", "aff");
        UhCasAttributes attributes = new UhCasAttributes(map);

        assertThat(attributes.getMap().size(), equalTo(5));
        assertThat(attributes.getUid(), equalTo("duckart"));
        assertThat(attributes.getUhUuid(), equalTo("666666"));
        assertThat(attributes.getName(), equalTo("Frank"));
        assertThat(attributes.getMail().get(0), equalTo("frank@example.com"));
        assertThat(attributes.getAffiliation().get(0), equalTo("aff"));

        assertThat(attributes.toString(), containsString("uid=duckart"));
    }

}
