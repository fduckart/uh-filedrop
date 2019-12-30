package edu.hawaii.its.filedrop.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Dates {

    // Constructor
    private Dates() {
        // empty constructor
    }

    public static LocalDate add(LocalDate date, int days) {
        return date.plusDays(days);
    }

    private static ZoneId zoneId() {
        return ZoneId.systemDefault();
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(zoneId()).toInstant());
    }
}
