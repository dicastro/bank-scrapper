package com.diegocastroviadero.bankscrapper.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum User {
    DIE("Diego");

    private final String name;
}
