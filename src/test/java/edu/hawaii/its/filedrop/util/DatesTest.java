package edu.hawaii.its.filedrop.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;

public class DatesTest {

    @Test
    public void addTest() {
        LocalDateTime date = LocalDateTime.of(1968, 11, 22, 1, 1, 1);
        assertThat(date.getDayOfWeek(), equalTo(DayOfWeek.FRIDAY));
        date = Dates.add(date, 5);
        assertThat(date.getDayOfMonth(), equalTo(27));
        assertThat(date.getDayOfWeek(), equalTo(DayOfWeek.WEDNESDAY));
    }

    @Test
    public void addMinutesTest() {
        LocalDateTime date0 = LocalDateTime.of(1968, 11, 22, 10, 24, 1);
        LocalDateTime date1 = LocalDateTime.of(1968, 11, 22, 10, 28, 1);

        assertThat(date0.getMinute(), equalTo(24));
        assertThat(date1.getMinute(), equalTo(28));

        LocalDateTime date2 = Dates.addMinutes(date0, 4);
        assertThat(date2.getMinute(), equalTo(28));
        assertThat(date2.getHour(), equalTo(10));
        assertThat(date2.getSecond(), equalTo(1));
        assertThat(date2.getDayOfMonth(), equalTo(22));
        assertThat(date2.getMonthValue(), equalTo(11));
        assertThat(date2.getYear(), equalTo(1968));
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
