package com.diegocastroviadero.bankscrapper.utils.console;

public class ArgumentReaderProvider {
    public static ArgumentReader getInstance() {
        final ArgumentReader instance;

        if (ConsoleArgumentReader.isAvailable()) {
            instance = new ConsoleArgumentReader();
        } else {
            instance = new ScannerArgumentReader();
        }

        return instance;
    }
}
