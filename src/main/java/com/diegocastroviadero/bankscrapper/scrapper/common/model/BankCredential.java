package com.diegocastroviadero.bankscrapper.scrapper.common.model;

import com.diegocastroviadero.bankscrapper.model.Bank;
import lombok.Getter;

@Getter
public abstract class BankCredential {
    protected final Bank bank;

    public BankCredential(final Bank bank) {
        this.bank = bank;
    }

    public <T extends BankCredential> T castTo(final Class<T> clazz) {
        return (T) this;
    }
}
