package com.diegocastroviadero.bankscrapper.scrapper.common.utils;

import com.diegocastroviadero.bankscrapper.scrapper.common.model.DateFilter;
import com.diegocastroviadero.bankscrapper.model.SyncType;

public class ScrapperUtils {
    public static DateFilter newDateFilterFrom(final SyncType syncType) {
        return new DateFilter(syncType.getStartDate(), syncType.getEndDate());
    }
}
