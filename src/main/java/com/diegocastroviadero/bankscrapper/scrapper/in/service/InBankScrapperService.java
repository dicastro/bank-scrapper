package com.diegocastroviadero.bankscrapper.scrapper.in.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.SyncType;
import com.diegocastroviadero.bankscrapper.model.User;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.Account;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.DateFilter;
import com.diegocastroviadero.bankscrapper.scrapper.common.service.BankScrapperService;
import com.diegocastroviadero.bankscrapper.scrapper.common.service.DriverProvider;
import com.diegocastroviadero.bankscrapper.scrapper.common.utils.ScrapperUtils;
import com.diegocastroviadero.bankscrapper.scrapper.in.console.InBankCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InBankScrapperService implements BankScrapperService {
    private final Random RANDOM = new Random();
    private final Integer ONE_SECOND = 1000;
    private final Integer TEN_SECONDS = 10000;
    private final Duration TEN_SECONDS_DUR = ofSeconds(10);
    private final Duration FIVE_MINUTES_DUR = ofMinutes(5);
    private final int MAX_LOGIN_RETRIES = 6;

    private final List<By> PRODUCT_BOX_BY_CHAIN = List.of(
            By.cssSelector("ing-app-es"),
            By.cssSelector("main > [basepath='overall-position']"),
            By.cssSelector("products-layout"),
            By.cssSelector("products-area"),
            By.cssSelector("product-box")
    );

    private final ScrappingProperties properties;

    private final DriverProvider driverProvider;

    private Map<String, String> mappings;

    @Override
    public Bank getBank() {
        return Bank.IN;
    }

    @Override
    public boolean applies(Bank bank) {
        return bank == getBank();
    }

    @Override
    public void scrap(final User user, final BankCredential bankCredential, final SyncType syncType, final Boolean whatIfMode) {
        initMappings(user);

        final DateFilter dateFilter = ScrapperUtils.newDateFilterFrom(syncType);

        log.info("Scrapping data from {} from {} to {} ...", getBank().getDescription(), dateFilter.getFrom(), dateFilter.getTo());

        if (whatIfMode) {
            log.info("WhatIf mode was activated: scrapping is not done");
        } else {
            RemoteWebDriver driver = null;

            try {
                driver = driverProvider.getDriver();

                login(driver, bankCredential.castTo(InBankCredential.class));

                final List<Account> accounts = getUserAccounts(driver);

                for (final Account account : accounts) {
                    log.debug("Downloading movements of account: {}", account.getNumber());

                    getAccountMovements(driver, account, dateFilter);
                }
            } catch (Exception e) {
                log.error("Error while scrapping data from {}", getBank().getDescription(), e);
            } finally {
                if (driver != null) {
                    driver.quit();
                }
            }
        }
    }

    private void initMappings(final User user) {
        this.mappings = System.getenv().keySet().stream()
                .filter(k -> k.matches(String.format("MAPPING_%s_%s_\\d+", user.name(), getBank().name())))
                .sorted()
                .map(k -> System.getenv(k).split(">"))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

    private void login(final RemoteWebDriver driver, final InBankCredential bankCredential) {
        driver.navigate().to("https://ing.ingdirect.es/app-login/");

        rejectCookies(driver);

        // username
        final WebElement documentNumberInput = getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input"),
                By.name("documentNumberInput")
        ));

        focusAndSendHumanKeys(driver, documentNumberInput, bankCredential.getUsername());

        // day
        final WebElement dayInput = getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input-date"),
                By.cssSelector("#input_day")
        ));

        focusAndSendHumanKeys(driver, dayInput, String.format("%02d", bankCredential.getBirthDate().getDayOfMonth()));

        // month
        final WebElement monthInput = getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input-date"),
                By.cssSelector("#input_month")
        ));

        focusAndSendHumanKeys(driver, monthInput, String.format("%02d", bankCredential.getBirthDate().getMonthValue()));

        // year
        final WebElement yearInput = getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input-date"),
                By.cssSelector("#input_year")
        ));

        focusAndSendHumanKeys(driver, yearInput, String.format("%d", bankCredential.getBirthDate().getYear()));

        // continue
        clickContinue(driver);

        // pin
        final WebElement pinContainer = waitUntilVisibilityOfShadowElement(driver, TEN_SECONDS_DUR, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-pin"),
                By.cssSelector("ing-uic-login-sca-es-el-pinpad"),
                By.cssSelector(".c-pinpad")));

        final List<WebElement> passPositions = pinContainer
                .findElements(By.cssSelector("div.c-pinpad__secret-positions__position"));

        waitMillis(ONE_SECOND);

        IntStream.range(0, passPositions.size())
                .map(i -> {
                    var blank = passPositions.get(i).getAttribute("class")
                            .contains("c-pinpad__secret-positions__position--selectable");

                    return blank ? i : -1;
                })
                .filter(i -> i > -1)
                .forEach(i -> {
                    log.debug("Filling position {} of password", i);

                    pinContainer
                            .findElement(By.xpath(String.format(".//li/span[text()='%s']", bankCredential.getPassword().charAt(i))))
                            .click();

                    waitMillis(ONE_SECOND);
                });

        // wait for app approval
        WebElement confimationMessageTitle = null;

        try {
            confimationMessageTitle = waitUntilVisibilityOfShadowElement(driver, TEN_SECONDS_DUR, List.of(
                    By.cssSelector("ing-app-login-sca-es"),
                    By.cssSelector("ing-orange-login-sca-es"),
                    By.cssSelector("ing-uic-login-sca-es-step-push"),
                    By.cssSelector("ing-uic-login-sca-es-el-header"),
                    By.cssSelector("h1")));

            if (!StringUtils.equals("Acceso seguro", confimationMessageTitle.getText())) {
                confimationMessageTitle = null;
            }
        } catch (TimeoutException ignored) {
            log.info("No app confirmation required to access");
        }

        if (null != confimationMessageTitle) {
            log.warn("App confirmation required to access. Check bank application. Waiting for approval...");

            WebDriverWait longWait = new WebDriverWait(driver, FIVE_MINUTES_DUR);
            longWait.until(stalenessOf(confimationMessageTitle));

            log.info("Access granted!");
        }
    }

    private WebElement getShadowedElement(final RemoteWebDriver driver, final List<By> byChain) {
        return getShadowedElementBase(driver, byChain)
                .findElement(byChain.getLast());
    }

    private List<WebElement> getShadowedElements(final RemoteWebDriver driver, final List<By> byChain) {
        return getShadowedElementBase(driver, byChain)
                .findElements(byChain.getLast());
    }

    private SearchContext getShadowedElementBase(final RemoteWebDriver driver, final List<By> byChain) {
        SearchContext sc = driver.findElement(By.tagName("body"));

        for (By by : byChain.subList(0, byChain.size() - 1)) {
            sc = sc.findElement(by).getShadowRoot();
        }

        return sc;
    }

    private WebElement waitUntilVisibilityOfShadowElement(final RemoteWebDriver driver, Duration duration, final List<By> byChain) {
        WebDriverWait wait = new WebDriverWait(driver, duration);

        SearchContext sc = driver.findElement(By.tagName("body"));

        for (By by : byChain.subList(0, byChain.size() - 1)) {
            sc = wait.until(presenceOfElement(sc, by)).getShadowRoot();
        }

        return wait.until(visibilityOf(sc.findElement(byChain.getLast())));
    }

    private List<WebElement> waitUntilVisibilityOfAllShadowElements(final RemoteWebDriver driver, Duration duration, final List<By> byChain) {
        WebDriverWait wait = new WebDriverWait(driver, duration);

        SearchContext sc = driver.findElement(By.tagName("body"));

        for (By by : byChain.subList(0, byChain.size() - 1)) {
            sc = wait.until(presenceOfElement(sc, by)).getShadowRoot();
        }

        return wait.until(visibilityOfAllElements(sc.findElements(byChain.getLast())));
    }

    private boolean rejectCookies(final RemoteWebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);

        try {
            wait.until(presenceOfElementLocated(By.xpath("//div[contains(@aria-label, ' consentimiento')]")))
                    .findElement(By.id("didomi-notice-disagree-button")).click();

            return true;
        } catch (TimeoutException ignored) {
            log.debug("Timeout waiting for cookie overlay to appear");
            return false;
        }
    }

    private void clickContinue(final RemoteWebDriver driver) {
        int retries = 0;
        boolean neededTry = true;

        while (neededTry && retries < MAX_LOGIN_RETRIES) {
            log.debug("Clicking 'continuar' button ({}/{})...", retries + 1, MAX_LOGIN_RETRIES);

            getShadowedElement(driver, List.of(
                    By.cssSelector("ing-app-login-sca-es"),
                    By.cssSelector("ing-orange-login-sca-es"),
                    By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                    By.cssSelector("paper-button")
            )).click();

            try {
                log.debug("Checking if login has been blocked...");

                String messageText = waitUntilVisibilityOfShadowElement(driver, TEN_SECONDS_DUR, List.of(
                        By.cssSelector("ing-app-login-sca-es"),
                        By.cssSelector("ing-orange-login-sca-es"),
                        By.cssSelector("ing-uic-login-sca-es-el-message-box"),
                        By.cssSelector("div#messageBox")
                )).getText();

                if (StringUtils.equals("Por razones técnicas no podemos atenderte. Estamos trabajando para solucionarlo lo antes posible. Disculpa las molestias.", messageText)) {
                    log.warn("Login has been blocked!");
                    retries++;

                    long waitTime = retries * 10000L;
                    log.debug("Waiting {} millis before clicking again 'continuar' button ...", waitTime);
                    waitMillis(waitTime);
                } else {
                    log.error("There was an unexpected error after clicking 'continuar': {}", messageText);

                    throw new RuntimeException("There was an unexpected error after clicking 'continuar'");
                }
            } catch (TimeoutException ignored) {
                log.info("Login has been unblocked!");
                neededTry = false;
            }
        }
    }

    private void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    private ExpectedCondition<WebElement> presenceOfElement(final SearchContext sc, final By by) {
        return driver -> {
            try {
                return sc.findElement(by);
            } catch (NoSuchElementException ignored) {
                return null;
            }
        };
    }

    private List<Account> getUserAccounts(final RemoteWebDriver driver) {
        final List<WebElement> productBoxes = waitUntilVisibilityOfAllShadowElements(driver, TEN_SECONDS_DUR, PRODUCT_BOX_BY_CHAIN);

        return productBoxes.stream()
                .map(sc -> sc.getShadowRoot().findElement(By.cssSelector("div.description > p")))
                .map(WebElement::getText)
                .filter(StringUtils::isNotEmpty)
                .map(rawAccount -> new Account(rawAccount, accountCleanup))
                .filter(a -> mappings.keySet().stream().anyMatch(mp -> a.getNumber().endsWith(mp)))
                .collect(Collectors.toList());
    }

    private final Function<String, String> accountCleanup = (rawAccount) -> rawAccount
            .trim()
            .replaceAll("\\*", "");

    private void getAccountMovements(final RemoteWebDriver driver, final Account account, final DateFilter dateFilter) {
        WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);

        // FIXME: there is an issue and this code runs before the element is clickable (the wait before this method is not working properly)
        // easy fix would be waiting 60s (but it could also fail)
        getShadowedElements(driver, PRODUCT_BOX_BY_CHAIN).stream()
                .map(sc -> sc.getShadowRoot().findElement(By.cssSelector("div.description")))
                .filter(productBox -> StringUtils.equals(
                        account.getRawNumber(),
                        productBox.findElement(By.cssSelector("p")).getText()))
                .findFirst()
                .ifPresent(we -> wait.until(elementToBeClickable(we)).click());

        wait.until(visibilityOfElementLocated(By.cssSelector("div.transactions-grid-container")));

        String dateValue = wait.until(visibilityOfElementLocated(By.cssSelector("span.date-navigator-label"))).getText();

        // FIXME: month should not be hardcoded, it should be extracted from dateFilter
        while (!StringUtils.equals("Febrero 2024", dateValue)) {
            wait.until(elementToBeClickable(By.cssSelector("a.navigate-back"))).click();

            dateValue = wait.until(visibilityOfElementLocated(By.cssSelector("span.date-navigator-label"))).getText();
        }

        wait.until(elementToBeClickable(By.cssSelector("a#export-movements-excel-ico"))).click();

        log.debug("Downloading... waiting for file to be downloaded ...");

        File downloadedFile = new FluentWait<>(properties.getDownloadPath().toFile())
                .withTimeout(ofSeconds(10))
                .ignoring(Exception.class)
                .pollingEvery(ofSeconds(2))
                .until(downloadPath -> Arrays.stream(Optional.ofNullable(downloadPath.listFiles()).orElse(new File[0]))
                            .filter(file -> file.getName().matches("[a-zA-Z0-9\\-]{8}.xls") && file.canWrite())
                            .findFirst()
                            .orElse(null));

        waitMillis(ONE_SECOND);

        final String renamedFile = String.format("%s-%s.xls",
                mappings.entrySet().stream()
                        .filter(e -> account.getNumber().endsWith(e.getKey()))
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse("NOTFOUND"),
                dateFilter.getTo().format(DateTimeFormatter.ofPattern("yyyyMM")));

        log.debug("Downloaded file! Renaming from {} to {}", downloadedFile.getName(), renamedFile);

        try {
            Files.move(
                    downloadedFile.toPath(),
                    properties.getDownloadPath().resolve(renamedFile),
                    REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error while renaming downloaded Movements.xls file to {}", renamedFile, e);
        }

        waitMillis(ONE_SECOND);
    }

    private void focusAndSendHumanKeys(final RemoteWebDriver driver, final WebElement element, final String text) {
        driver.executeScript("arguments[0].focus();", element);
        waitMillis(ONE_SECOND);

        text.chars()
                .mapToObj(Character::toString)
                .forEach(letter -> {
                    waitMillis((long) (RANDOM.nextGaussian() * 15 + 100));
                    element.sendKeys(letter);
                });
    }
}
