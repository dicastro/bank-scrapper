package com.diegocastroviadero.bankscrapper.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

public class Utils {
    public static final ZoneId UTC = ZoneId.of("UTC");

    public static YearMonth getYearMonth(final Instant instant) {
        return YearMonth.from(instant.atZone(UTC).toLocalDate());
    }

    public static String getDayOfMonthAsString(final LocalDate date) {
        return Integer.toString(date.getDayOfMonth());
    }

    public static String getMonthAsString(final LocalDate date) {
        return Integer.toString(date.getMonthValue());
    }

    public static String getYearAsString(final LocalDate date) {
        return Integer.toString(date.getYear());
    }
}
