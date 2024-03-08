package com.diegocastroviadero.bankscrapper.scrapper.kb.console;

import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.model.Bank;
import lombok.Getter;

@Getter
public class KbBankCredential extends BankCredential {
    private final String username;
    private final String password;

    public KbBankCredential(final Bank bank, final String username, final String password) {
        super(bank);
        this.username = username;
        this.password = password;
    }
}
