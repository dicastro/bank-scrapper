package com.diegocastroviadero.bankscrapper.scrapper.common.model;

import lombok.Getter;

import java.util.function.Function;

@Getter
public class Account {
    private final String rawNumber;
    private final String number;

    public Account(final String rawNumber, final Function<String, String> cleanup) {
        this.rawNumber = rawNumber;
        this.number = cleanup.apply(rawNumber);
    }
}
