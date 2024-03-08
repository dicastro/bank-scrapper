package com.diegocastroviadero.bankscrapper.scrapper.in.console;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredentialReader;
import com.diegocastroviadero.bankscrapper.utils.console.ArgumentReader;
import com.diegocastroviadero.bankscrapper.utils.console.ArgumentReaderProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.diegocastroviadero.bankscrapper.utils.console.ArgumentReader.DATE_FORMAT;

@Service
public class InBankCredentialReader implements BankCredentialReader {

    private final ArgumentReader argumentReader = ArgumentReaderProvider.getInstance();

    @Override
    public Bank getBank() {
        return Bank.IN;
    }

    @Override
    public boolean applies(final Bank bank) {
        return bank == getBank();
    }

    @Override
    public BankCredential readBankCredentials() {
        final String username = argumentReader.readString(String.format("Username for '%s' > ", getBank()));
        final String password = argumentReader.readPassword(String.format("Password for '%s' > ", getBank()));
        final LocalDate birthDate = argumentReader.readDate(String.format("What is your birth date? (%s) > ", DATE_FORMAT));

        return new InBankCredential(getBank(), username, password, birthDate);
    }
}
