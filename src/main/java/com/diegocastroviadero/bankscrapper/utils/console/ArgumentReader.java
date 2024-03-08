package com.diegocastroviadero.bankscrapper.utils.console;

import java.nio.file.Path;
import java.time.LocalDate;

public interface ArgumentReader {
    String DATE_FORMAT = "dd/MM/yyyy";

    String readString(final String message);
    String readPassword(final String message);
    Path readDir(final String message) throws IllegalArgumentException;
    LocalDate readDate(String message);
}