package com.diegocastroviadero.bankscrapper.model;

import com.diegocastroviadero.bankscrapper.utils.Utils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum SyncType {
    PAST_ONE_MONTH(1),
    PAST_TWO_MONTHS(2);

    private final int monthDiff;

    public LocalDate getStartDate() {
        final ZonedDateTime today = Utils.now();

        final YearMonth now = YearMonth.from(today);

        return now.minusMonths(monthDiff).atDay(1);
    }

    public LocalDate getEndDate() {
        final ZonedDateTime today = Utils.now();

        final YearMonth now = YearMonth.from(today);

        return now.minusMonths(monthDiff).atEndOfMonth();
    }
}
