package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class OfficeTest {

    private Office office;

    @Before
    public void setUp() {
        office = new Office();
    }

    @Test
    public void accessors() {
        assertNull(office.getId());
        assertNull(office.getCampus());
        assertNull(office.getCampusId());
        assertNull(office.getDescription());

        office.setId(666);
        office.setCampus(new Campus());
        office.setCampusId(1);
        office.setDescription(null);

        assertThat(office.getId(), equalTo(666));
        assertThat(office.getOfficeId(), equalTo(666));
        assertThat(office.getCampusId(), equalTo(1));
        assertNotNull(office.getCampus());
        assertThat(office.getDescription(), equalTo(""));

        Campus campus = new Campus();
        campus.setId(456);
        campus.setDescription("some campus");
        office.setCampus(campus);
        office.setDescription("The Office");

        assertThat(office.getCampusId(), equalTo(456));
        assertNotNull(office.getCampus());
        assertThat(office.getDescription(), equalTo("The Office"));
        assertThat(office.getLongDescription(), equalTo("some campus: The Office"));

        assertThat(office.getSortId(), equalTo(null));
        office.setSortId(1);
        assertThat(office.getSortId(), equalTo(1));

        office = new Office(null);
        assertThat(office.getCampusId(), equalTo(null));

        office.setCampus(new Campus());
        assertNotNull(office.getCampus());
        assertThat(office.getCampusId(), equalTo(null));

        office.setCampus(new Campus(321));
        assertNotNull(office.getCampus());
        assertThat(office.getCampusId(), equalTo(321));

        office.setCampus(null);
        assertNull(office.getCampus());
    }

    @Test
    public void testEquals() {
        Office o1 = new Office();
        assertThat(o1, is(equalTo(o1)));

        Office o2 = null;
        assertThat(o1, is(not(equalTo(o2))));

        o2 = new Office();
        assertThat(o1, is(equalTo(o2)));

        o1.setId(1);
        assertThat(o1, is(not(equalTo(o2))));

        o2.setId(1);
        assertThat(o1, is(equalTo(o2)));

        o2.setId(2);
        assertThat(o1, is(not(equalTo(o2))));

        o1.setId(null);
        o2.setId(2);
        assertThat(o1, is(not(equalTo(o2))));

        // Wrong class.
        assertThat(o1, is(not(equalTo(new String()))));
    }

    @Test
    public void testHashCode() {
        assertThat(office.hashCode(), equalTo(31));

        office.setId(7);
        assertThat(office.hashCode(), equalTo(38));
    }

    @Test
    public void testToString() {
        assertThat(office.toString(), containsString("Office ["));

        office.setId(678);
        assertThat(office.toString(), containsString("[id=678"));

        office.setCampusId(123);
        assertThat(office.toString(), containsString("campusId=123"));
    }
}
