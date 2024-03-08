package com.diegocastroviadero.bankscrapper.scrapper.in.console;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InBankCredential extends BankCredential {
    private final String username;
    private final String password;
    private final LocalDate birthDate;

    public InBankCredential(final Bank bank, final String username, final String password, final LocalDate birthDate) {
        super(bank);
        this.username = username;
        this.password = password;
        this.birthDate = birthDate;
    }
}
