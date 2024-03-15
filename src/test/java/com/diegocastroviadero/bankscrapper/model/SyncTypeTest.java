package com.diegocastroviadero.bankscrapper.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// FIXME: fix these tests as the SyncType logic has changed
@Disabled
class SyncTypeTest {
    @Test
    void givenSyncPastOneMonth_whenGetStartAndEndDateAtFirstOfMonth_itWorks() {
        // Given
        SyncType syncType = SyncType.PAST_ONE_MONTH;

        final Instant now = ZonedDateTime.of(2021, 5, 1, 12, 0, 0, 0, ZoneId.of("UTC")).toInstant();

        final LocalDate expectedStartDate = LocalDate.of(2021, 4, 1);
        final LocalDate expectedEndDate = LocalDate.of(2021, 4, 30);

        // When
        final LocalDate startDate = syncType.getStartDate(now);
        final LocalDate endDate = syncType.getEndDate(now);

        // Then
        assertEquals(expectedStartDate, startDate);
        assertEquals(expectedEndDate, endDate);
    }

    @Test
    void givenSyncPastTwoMonths_whenGetStartAndEndDateAtFirstOfMonth_itWorks() {
        // Given
        SyncType syncType = SyncType.PAST_TWO_MONTHS;

        final Instant now = ZonedDateTime.of(2021, 9, 1, 12, 0, 0, 0, ZoneId.of("UTC")).toInstant();

        final LocalDate expectedStartDate = LocalDate.of(2021, 7, 1);
        final LocalDate expectedEndDate = LocalDate.of(2021, 8, 31);

        // When
        final LocalDate startDate = syncType.getStartDate(now);
        final LocalDate endDate = syncType.getEndDate(now);

        // Then
        assertEquals(expectedStartDate, startDate);
        assertEquals(expectedEndDate, endDate);
    }

    @Test
    void givenSyncPastOneMonth_whenGetStartAndEndDateAtMiddleOfMonth_itWorks() {
        // Given
        SyncType syncType = SyncType.PAST_ONE_MONTH;

        final Instant now = ZonedDateTime.of(2021, 5, 20, 12, 0, 0, 0, ZoneId.of("UTC")).toInstant();

        final LocalDate expectedStartDate = LocalDate.of(2021, 4, 1);
        final LocalDate expectedEndDate = LocalDate.of(2021, 4, 30);

        // When
        final LocalDate startDate = syncType.getStartDate(now);
        final LocalDate endDate = syncType.getEndDate(now);

        // Then
        assertEquals(expectedStartDate, startDate);
        assertEquals(expectedEndDate, endDate);
    }

    @Test
    void givenSyncPastTwoMonths_whenGetStartAndEndDateAtMiddleOfMonth_itWorks() {
        // Given
        SyncType syncType = SyncType.PAST_TWO_MONTHS;

        final Instant now = ZonedDateTime.of(2021, 9, 20, 12, 0, 0, 0, ZoneId.of("UTC")).toInstant();

        final LocalDate expectedStartDate = LocalDate.of(2021, 7, 1);
        final LocalDate expectedEndDate = LocalDate.of(2021, 8, 31);

        // When
        final LocalDate startDate = syncType.getStartDate(now);
        final LocalDate endDate = syncType.getEndDate(now);

        // Then
        assertEquals(expectedStartDate, startDate);
        assertEquals(expectedEndDate, endDate);
    }
}