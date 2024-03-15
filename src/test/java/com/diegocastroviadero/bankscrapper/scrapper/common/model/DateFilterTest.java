package com.diegocastroviadero.bankscrapper.scrapper.common.model;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateFilterTest {

    @Test
    void testYearMonthList1() {
        // Given
        final LocalDate startDate = LocalDate.of(2021, 2, 1);
        final LocalDate endDate = LocalDate.of(2021, 2, 28);

        final List<YearMonth> expectedYearMonthList = List.of(
                YearMonth.of(2021, Month.FEBRUARY)
        );

        // When
        final List<YearMonth> yearMonthList = new DateFilter(startDate, endDate).getYearMonthList();

        // Then
        assertEquals(expectedYearMonthList, yearMonthList);
    }

    @Test
    void testYearMonthList2() {
        // Given
        final LocalDate startDate = LocalDate.of(2021, 2, 1);
        final LocalDate endDate = LocalDate.of(2021, 5, 31);

        final List<YearMonth> expectedYearMonthList = List.of(
                YearMonth.of(2021, Month.MAY),
                YearMonth.of(2021, Month.APRIL),
                YearMonth.of(2021, Month.MARCH),
                YearMonth.of(2021, Month.FEBRUARY)
        );

        // When
        final List<YearMonth> yearMonthList = new DateFilter(startDate, endDate).getYearMonthList();

        // Then
        assertEquals(expectedYearMonthList, yearMonthList);
    }

    @Test
    void testYearMonthList3() {
        // Given
        final LocalDate startDate = LocalDate.of(2021, 2, 1);
        final LocalDate endDate = LocalDate.of(2021, 3, 31);

        final List<YearMonth> expectedYearMonthList = List.of(
                YearMonth.of(2021, Month.MARCH),
                YearMonth.of(2021, Month.FEBRUARY)
        );

        // When
        final List<YearMonth> yearMonthList = new DateFilter(startDate, endDate).getYearMonthList();

        // Then
        assertEquals(expectedYearMonthList, yearMonthList);
    }

    @Test
    void testYearMonthList4() {
        // Given
        final LocalDate startDate = LocalDate.of(2020, 12, 1);
        final LocalDate endDate = LocalDate.of(2021, 3, 31);

        final List<YearMonth> expectedYearMonthList = List.of(
                YearMonth.of(2021, Month.MARCH),
                YearMonth.of(2021, Month.FEBRUARY),
                YearMonth.of(2021, Month.JANUARY),
                YearMonth.of(2020, Month.DECEMBER)
        );

        // When
        final List<YearMonth> yearMonthList = new DateFilter(startDate, endDate).getYearMonthList();

        // Then
        assertEquals(expectedYearMonthList, yearMonthList);
    }
}