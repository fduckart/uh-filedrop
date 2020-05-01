package edu.hawaii.its.filedrop.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class Dates {

    // Constructor.
    private Dates() {
        // Empty.
    }

    public static LocalDateTime add(LocalDateTime dateTime, int days) {
        return dateTime.plusDays(days);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        return dateTime.plus(minutes, ChronoUnit.MINUTES);
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
