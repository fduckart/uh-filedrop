package edu.hawaii.its.filedrop.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;

public class DatesTest {

    @Test
    public void addTest() {
        LocalDate date = LocalDate.of(1968, 11, 22);
        assertThat(date.getDayOfWeek(), equalTo(DayOfWeek.FRIDAY));
        date = Dates.add(date, 5);
        assertThat(date.getDayOfMonth(), equalTo(27));
        assertThat(date.getDayOfWeek(), equalTo(DayOfWeek.WEDNESDAY));
    }

    @Test
    public void toDateTest() {
        LocalDate localDate = LocalDate.of(2020, 2, 14);
        Date date = Dates.toDate(localDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertThat(localDate.getYear(), equalTo(calendar.get(Calendar.YEAR)));
        assertThat(localDate.getMonthValue(), equalTo(calendar.get(Calendar.MONTH) + 1));
        assertThat(localDate.getDayOfMonth(), equalTo(calendar.get(Calendar.DAY_OF_MONTH)));

        assertNull(Dates.toDate(null));
    }

}
