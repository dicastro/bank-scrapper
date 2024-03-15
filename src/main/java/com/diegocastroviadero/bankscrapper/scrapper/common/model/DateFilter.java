package com.diegocastroviadero.bankscrapper.scrapper.common.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.reverseOrder;

@Getter
@ToString
public class DateFilter {
    private final LocalDate from;
    private final LocalDate to;
    private final List<YearMonth> yearMonthList;

    public DateFilter(final LocalDate start, final LocalDate end) {
        from = start;
        to = end;
        yearMonthList = initializeYearMonthList();
    }

    private List<YearMonth> initializeYearMonthList() {
        YearMonth currentYearMonth = YearMonth.from(from);
        final YearMonth endYearMonth = YearMonth.from(to);

        final List<YearMonth> yearMonthList = new ArrayList<>();

        do {
            yearMonthList.add(currentYearMonth);
            currentYearMonth = currentYearMonth.plusMonths(1);
        } while (currentYearMonth.isBefore(endYearMonth) || currentYearMonth.equals(endYearMonth));

        return yearMonthList.stream().sorted(reverseOrder()).toList();
    }
}
