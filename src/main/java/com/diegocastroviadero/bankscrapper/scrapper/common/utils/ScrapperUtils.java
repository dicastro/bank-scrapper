package com.diegocastroviadero.bankscrapper.scrapper.common.utils;

import com.diegocastroviadero.bankscrapper.scrapper.common.model.DateFilter;
import com.diegocastroviadero.bankscrapper.model.SyncType;

import java.time.Instant;

public class ScrapperUtils {
    public static DateFilter newDateFilterFrom(final SyncType syncType, final Instant now) {
        return new DateFilter(syncType.getStartDate(now), syncType.getEndDate(now));
    }
}
