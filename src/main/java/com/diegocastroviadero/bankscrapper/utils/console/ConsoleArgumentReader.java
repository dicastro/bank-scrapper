package com.diegocastroviadero.bankscrapper.utils.console;

import java.io.Console;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConsoleArgumentReader implements ArgumentReader {
    private final Console in = System.console();

    public static boolean isAvailable() {
        return null != System.console();
    }

    @Override
    public String readString(String message) {
        // TODO: validate read string is not blank
        return in.readLine(message);
    }

    @Override
    public String readPassword(String message) {
        // TODO: validate read string is not blank
        return new String(in.readPassword(message));
    }

    @Override
    public Path readDir(String message) throws IllegalArgumentException {
        // TODO: validate is dir and exists
        return Paths.get(readString(message));
    }

    @Override
    public LocalDate readDate(String message) {
        return LocalDate.parse(readString(message), DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
}
