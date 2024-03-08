package com.diegocastroviadero.bankscrapper.scrapper.common.model;

import com.diegocastroviadero.bankscrapper.model.Bank;

import java.io.IOException;

public interface BankCredentialReader {
    Bank getBank();
    boolean applies(final Bank bank);

    BankCredential readBankCredentials();
}
