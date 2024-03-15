package com.diegocastroviadero.bankscrapper.model;

import com.diegocastroviadero.bankscrapper.utils.Utils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum SyncType {
    PAST_ONE_MONTH(1),
    PAST_TWO_MONTHS(2);

    private final int monthDiff;

    public LocalDate getStartDate(Instant now) {
        final YearMonth nowYM = Utils.getYearMonth(now);

        return nowYM.minusMonths(monthDiff).atDay(1);
    }

    public LocalDate getEndDate(Instant now) {
        final YearMonth nowYM = Utils.getYearMonth(now);

        return nowYM.minusMonths(1).atEndOfMonth();
    }
}
