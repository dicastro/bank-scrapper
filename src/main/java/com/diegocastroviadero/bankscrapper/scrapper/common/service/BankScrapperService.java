package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.SyncType;
import com.diegocastroviadero.bankscrapper.model.User;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;

import java.time.Instant;

public interface BankScrapperService {
    Bank getBank();

    boolean applies(final Bank bank);

    void scrap(final User user, final BankCredential bankCredential, final SyncType syncType, final Instant now, final Boolean whatIfMode);
}
