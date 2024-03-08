package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredentialReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BankCredentialReaderProvider {
    private final List<BankCredentialReader> bankCredentialReaders;

    public Optional<BankCredentialReader> getBankCredentialReader(final Bank bank) {
        return bankCredentialReaders.stream()
                .filter(bankCredentialReader -> bankCredentialReader.applies(bank))
                .findFirst();
    }
}
