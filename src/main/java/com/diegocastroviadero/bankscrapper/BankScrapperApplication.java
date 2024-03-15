package com.diegocastroviadero.bankscrapper;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.SyncType;
import com.diegocastroviadero.bankscrapper.model.User;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.common.service.BankCredentialReaderProvider;
import com.diegocastroviadero.bankscrapper.scrapper.common.service.BankScrapperServiceProvider;
import com.diegocastroviadero.bankscrapper.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.diegocastroviadero.bankscrapper.utils.Utils.UTC;

@AllArgsConstructor
@Slf4j
@SpringBootApplication
public class BankScrapperApplication implements CommandLineRunner {
    private static final String USER_OPTION = "u";
    private static final String BANK_OPTION = "b";
    private static final String SYNC_TYPE_OPTION = "t";
    private static final String WHATIF_MODE_OPTION = "w";

    private final Clock clock = Clock.system(UTC);

    private final ScrappingProperties properties;
    private final BankScrapperServiceProvider bankScrapperServiceProvider;
    private final BankCredentialReaderProvider bankCredentialReaderProvider;

    public static void main(String[] args) {
        SpringApplication.run(BankScrapperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final Options options = new Options();
        options.addOption(USER_OPTION, "user", Boolean.TRUE, "User");
        options.addOption(BANK_OPTION, "bank", Boolean.TRUE, "Bank");
        options.addOption(SYNC_TYPE_OPTION, "sync-type", Boolean.TRUE, "Sync type");
        options.addOption(WHATIF_MODE_OPTION, "what-if", Boolean.FALSE, "WhatIf mode");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;

        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            throw new RuntimeException("Error while parsing arguments", e);
        }

        final boolean whatIfMode = cmd.hasOption(WHATIF_MODE_OPTION);

        final User user;

        if (cmd.hasOption(USER_OPTION)) {
            final String rawUser = cmd.getOptionValue(USER_OPTION);

            if (EnumUtils.isValidEnum(User.class, rawUser)) {
                user = EnumUtils.getEnum(User.class, rawUser);

                log.debug("User argument received {}", user);
            } else {
                user = null;
                endWithError(String.format("'%s' is not a valid user. Admitted values are: %s", rawUser, Stream.of(User.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "))));
            }
        } else {
            user = null;
            endWithError("'User' argument is required");
        }

        final Bank bank;

        if (cmd.hasOption(BANK_OPTION)) {
            final String rawBank = cmd.getOptionValue(BANK_OPTION);

            if (EnumUtils.isValidEnum(Bank.class, rawBank)) {
                bank = EnumUtils.getEnum(Bank.class, rawBank);

                log.debug("Bank argument received {}", bank);
            } else {
                bank = null;
                log.warn("'{}' is not a valid bank. All user banks will be processed. Admitted values are: {}", rawBank, Stream.of(User.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
            }
        } else {
            bank = null;
        }

        final SyncType syncType;

        if (cmd.hasOption(SYNC_TYPE_OPTION)) {
            final String rawSyncType = cmd.getOptionValue(SYNC_TYPE_OPTION);

            if (EnumUtils.isValidEnum(SyncType.class, rawSyncType)) {
                syncType = EnumUtils.getEnum(SyncType.class, rawSyncType);

                log.debug("Sync type argument received {}", syncType);
            } else {
                syncType = null;
                endWithError(String.format("'%s' is not a valid sync type. Admitted values are: %s", rawSyncType, Stream.of(SyncType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "))));
            }
        } else {
            syncType = null;
            endWithError("Sync type argument is required");
        }

        List<Bank> banksToProcess;

        if (null != bank) {
            // TODO: validate bank is available for the user
            banksToProcess = Collections.singletonList(bank);
        } else {
            banksToProcess = properties.getUserBanks().get(user);
        }

        Instant now = clock.instant();

        for (Bank currentBank : banksToProcess) {
            bankCredentialReaderProvider.getBankCredentialReader(currentBank)
                    .ifPresent(bankCredentialReader -> {
                        final BankCredential bankCredential = bankCredentialReader.readBankCredentials();

                        bankScrapperServiceProvider.getBankScrapperService(currentBank)
                                .ifPresent(bankScrapperService -> bankScrapperService.scrap(user, bankCredential, syncType, now, whatIfMode));
                    });
        }

        System.exit(0);
    }

    private void endWithError(final String errorMessage) {
        log.error(errorMessage);

        System.exit(1);
    }
}
