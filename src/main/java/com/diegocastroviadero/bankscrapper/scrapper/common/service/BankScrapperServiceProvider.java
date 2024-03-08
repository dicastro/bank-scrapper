package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.model.Bank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BankScrapperServiceProvider {
    private final List<BankScrapperService> bankScrapperServices;

    public Optional<BankScrapperService> getBankScrapperService(final Bank bank) {
        return this.bankScrapperServices.stream()
                .filter(bankScrapperService -> bankScrapperService.applies(bank))
                .findFirst();
    }
}
