package com.diegocastroviadero.bankscrapper.scrapper.common.model;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.SyncType;
import com.diegocastroviadero.bankscrapper.model.User;

public interface BankScrapperService {
    Bank getBank();

    boolean applies(final Bank bank);

    void scrap(final User user, final BankCredential bankCredential, final SyncType syncType, final Boolean whatIfMode);
}
