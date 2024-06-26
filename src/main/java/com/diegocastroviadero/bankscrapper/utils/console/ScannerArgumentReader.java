package com.diegocastroviadero.bankscrapper.utils.console;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ScannerArgumentReader implements ArgumentReader {
    private final Scanner in = new Scanner(System.in);

    public static boolean isAvailable() {
        return Boolean.TRUE;
    }

    @Override
    public String readString(String message) {
        // TODO: validate read string is not blank
        System.out.print(message);
        return in.nextLine();
    }

    @Override
    public String readPassword(String message) {
        // TODO: validate read string is not blank
        return readString(message);
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
