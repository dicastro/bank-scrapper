package com.diegocastroviadero.bankscrapper.scrapper.in.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.SyncType;
import com.diegocastroviadero.bankscrapper.model.User;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.Account;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankScrapperService;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.DateFilter;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

@Slf4j
@Service
@RequiredArgsConstructor
public class InBankScrapperService implements BankScrapperService {
    private final Integer ONE_SECOND = 1000;
    private final Duration TEN_SECONDS_DUR = ofSeconds(10);
    private final Duration FIVE_MINUTES_DUR = ofMinutes(5);

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

                    // TODO: getAccountMovements(driver, account, dateFilter);
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
        getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input"),
                By.name("documentNumberInput")
        )).sendKeys(bankCredential.getUsername());

        // day
        getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input-date"),
                By.cssSelector("#input_day")
        )).sendKeys(String.format("%02d", bankCredential.getBirthDate().getDayOfMonth()));

        // month
        getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input-date"),
                By.cssSelector("#input_month")
        )).sendKeys(String.format("%02d", bankCredential.getBirthDate().getMonthValue()));

        // year
        getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("ing-uic-login-sca-es-el-input-date"),
                By.cssSelector("#input_year")
        )).sendKeys(String.format("%d", bankCredential.getBirthDate().getYear()));

        // continue
        getShadowedElement(driver, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-credentials"),
                By.cssSelector("paper-button")
        )).click();

        // pin
        final WebElement pinContainer = waitUntilVisibilityOfShadowElement(driver, TEN_SECONDS_DUR, List.of(
                By.cssSelector("ing-app-login-sca-es"),
                By.cssSelector("ing-orange-login-sca-es"),
                By.cssSelector("ing-uic-login-sca-es-step-pin"),
                By.cssSelector("ing-uic-login-sca-es-el-pinpad"),
                By.cssSelector(".c-pinpad")));

        final List<WebElement> passPositions = pinContainer
                .findElements(By.cssSelector("div.c-pinpad__secret-positions__position"));

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
                            .findElement(By.xpath(String.format("//li/span[text()='%s']", bankCredential.getPassword().charAt(i))))
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
        final WebElement productsArea = waitUntilVisibilityOfShadowElement(driver, TEN_SECONDS_DUR, List.of(
                By.cssSelector("ing-app-es"),
                By.xpath("//*[starts-with(name(), 'overall-position-')]"),
                By.cssSelector("products-layout"),
                By.cssSelector("products-area")
        ));

        return productsArea.getShadowRoot()
                .findElements(By.xpath("//product-box//a[@class='link-unstyled']//div[@class='description']/p")).stream()
                .map(WebElement::getText)
                .filter(StringUtils::isNotEmpty)
                .map(rawAccount -> new Account(rawAccount, accountCleanup))
                .filter(a -> mappings.keySet().stream().anyMatch(mp -> a.getNumber().endsWith(mp)))
                .collect(Collectors.toList());
    }

    private final Function<String, String> accountCleanup = (rawAccount) -> rawAccount
            .trim()
            .replaceAll("\\*", "");
}
