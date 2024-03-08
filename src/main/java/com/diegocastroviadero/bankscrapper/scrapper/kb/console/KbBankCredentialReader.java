package com.diegocastroviadero.bankscrapper.scrapper.kb.console;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredentialReader;
import com.diegocastroviadero.bankscrapper.utils.console.ArgumentReader;
import com.diegocastroviadero.bankscrapper.utils.console.ArgumentReaderProvider;
import org.springframework.stereotype.Service;

@Service
public class KbBankCredentialReader implements BankCredentialReader {

    private final ArgumentReader argumentReader = ArgumentReaderProvider.getInstance();

    @Override
    public Bank getBank() {
        return Bank.KB;
    }

    @Override
    public boolean applies(final Bank bank) {
        return bank == getBank();
    }

    @Override
    public BankCredential readBankCredentials() {
        final String username = argumentReader.readString(String.format("Username for '%s' > ", getBank()));
        final String password = argumentReader.readPassword(String.format("Password for '%s' > ", getBank()));

        return new KbBankCredential(getBank(), username, password);
    }
}
