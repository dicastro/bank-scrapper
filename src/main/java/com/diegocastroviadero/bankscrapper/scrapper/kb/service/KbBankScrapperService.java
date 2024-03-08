package com.diegocastroviadero.bankscrapper.scrapper.kb.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.SyncType;
import com.diegocastroviadero.bankscrapper.model.User;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.Account;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.common.service.BankScrapperService;
import com.diegocastroviadero.bankscrapper.scrapper.common.model.DateFilter;
import com.diegocastroviadero.bankscrapper.scrapper.common.service.DriverProvider;
import com.diegocastroviadero.bankscrapper.scrapper.common.utils.ScrapperUtils;
import com.diegocastroviadero.bankscrapper.scrapper.kb.console.KbBankCredential;
import com.diegocastroviadero.bankscrapper.scrapper.kb.keyboard.Keyboard;
import com.diegocastroviadero.bankscrapper.scrapper.kb.keyboard.KeyboardManager;
import com.diegocastroviadero.bankscrapper.scrapper.kb.keyboard.UnparseableKeyboardException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfElementsToBeMoreThan;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbBankScrapperService implements BankScrapperService {
    private final Integer ONE_SECOND = 1000;
    private final Integer FIVE_SECONDS = 5000;

    private final Duration TEN_SECONDS_DUR = ofSeconds(10);

    private final ScrappingProperties properties;
    private final DriverProvider driverProvider;
    private final KeyboardManager keyboardManager;
    private Map<String, String> mappings;

    @Override
    public Bank getBank() {
        return Bank.KB;
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

                login(driver, bankCredential.castTo(KbBankCredential.class));

                final boolean switchedToTab = swithToTab(driver);

                if (switchedToTab) {
                    final List<Account> accounts = getUserAccounts(driver);

                    for (final Account account : accounts) {
                        log.debug("Downloading movements of account: {}", account.getNumber());

                        getAccountMovements(driver, account, dateFilter);
                    }

                    final List<Account> creditCards = getUserCreditCards(driver);

                    for (final Account creditCard : creditCards) {
                        log.debug("Downloading movements of credit card: {}", creditCard.getNumber());

                        getAccountMovements(driver, creditCard, dateFilter);
                    }

                    waitMillis(FIVE_SECONDS);
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

    private void login(final RemoteWebDriver driver, final KbBankCredential bankCredential) throws IOException, UnparseableKeyboardException {
        driver.navigate().to("https://portal.kutxabank.es/cs/Satellite/kb/es/particulares");

        driver.findElements(By.className("cookies-boton")).stream()
                .filter(WebElement::isDisplayed)
                .filter(webElement -> webElement.getText().equalsIgnoreCase("Aceptar todas"))
                .findFirst()
                .ifPresent(WebElement::click);

        final WebElement user = driver.findElement(By.id("usuario"));
        user.sendKeys(bankCredential.getUsername());

        final WebElement password = driver.findElement(By.id("password_PAS"));
        password.click();

        waitMillis(FIVE_SECONDS);

        final WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);
        final WebElement keyboardImg = wait.until(presenceOfElementLocated(By.id("tecladoImg")));

        final Point keyboardImgLocation = keyboardImg.getLocation();
        final Dimension keyboardImgSize = keyboardImg.getSize();

        final Path fullScreenshotPath = properties.getBanks().getKb().getKeyboardCache().getTmp().resolve("full_screenshot.png");

        final byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(fullScreenshotPath, screenshotBytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        final BufferedImage screenshotBufferedImage = ImageIO.read(fullScreenshotPath.toFile());
        final BufferedImage screenshotSection = screenshotBufferedImage.getSubimage(keyboardImgLocation.getX() + 1, keyboardImgLocation.getY(), keyboardImgSize.getWidth(), keyboardImgSize.getHeight());

        final Path keyboardScreenshotPath = properties.getBanks().getKb().getKeyboardCache().getTmp().resolve("keyboard_screenshot.png");
        ImageIO.write(screenshotSection, "png", keyboardScreenshotPath.toFile());

        fullScreenshotPath.toFile().deleteOnExit();

        final Keyboard keyboard = keyboardManager.parseKeyboard(keyboardScreenshotPath.toFile());

        final Actions actions = new Actions(driver);
        keyboard.getSequenceOfPassword(bankCredential.getPassword())
                .forEach(offset -> {
                    actions
                            .moveToElement(keyboardImg)
                            .moveByOffset(-(keyboardImgSize.getWidth() / 2), -(keyboardImgSize.getHeight() / 2))
                            .moveByOffset(offset.getX(), offset.getY())
                            .click().perform();
                    waitMillis(500);
                });

        log.debug("Pasword introduced !");

        waitMillis(ONE_SECOND);

        driver.findElement(By.id("enviar")).click();

        waitMillis(ONE_SECOND);
    }

    private void goToStart(final RemoteWebDriver driver) {
        final WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);
        wait.until(elementToBeClickable(By.xpath(("//div[@id = 'formMenuSuperior:PanelSuperior']//a[text() = 'Resumen']"))))
                .click();
    }

    // TODO: improve this method by getting the list of accounts from the dedicated section
    private List<Account> getUserAccounts(final RemoteWebDriver driver) {
        goToStart(driver);

        final WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);
        wait.until(numberOfElementsToBeMoreThan(By.xpath("//div[contains(@class, 'posiciones_tituloSeccion')]/span[text() = 'Cuentas']/../..//tr[contains(@class, 'posicicones_tablaListaContratosRow')]"), 0));

        return driver.findElements(By.xpath("//div[contains(@class, 'posiciones_tituloSeccion')]/span[text() = 'Cuentas']/../..//tr[contains(@class, 'posicicones_tablaListaContratosRow')]/td[contains(@class, 'posiciones_columna1Posicion')]")).stream()
                .map(WebElement::getText)
                .filter(StringUtils::isNotEmpty)
                .map(rawAccount -> new Account(rawAccount, accountCleanup))
                .filter(a -> mappings.keySet().stream().anyMatch(mp -> a.getNumber().endsWith(mp)))
                .collect(Collectors.toList());
    }

    // TODO: improve this method by getting the list of credit cards from the dedicated section
    private List<Account> getUserCreditCards(final RemoteWebDriver driver) {
        goToStart(driver);

        final WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);
        wait.until(numberOfElementsToBeMoreThan(By.xpath("//div[contains(@class, 'posiciones_tituloSeccion')]/span[text() = 'Tarjetas']/../..//tr[contains(@class, 'posicicones_tablaListaContratosRow')]"), 0));

        return driver.findElements(By.xpath("//div[contains(@class, 'posiciones_tituloSeccion')]/span[text() = 'Tarjetas']/../..//tr[contains(@class, 'posicicones_tablaListaContratosRow')]/td[contains(@class, 'posiciones_columna1Posicion')]")).stream()
                .map(WebElement::getText)
                .filter(StringUtils::isNotEmpty)
                .map(rawAccount -> new Account(rawAccount, accountCleanup))
                .filter(a -> mappings.keySet().stream().anyMatch(mp -> a.getNumber().endsWith(mp)))
                .collect(Collectors.toList());
    }

    private void getAccountMovements(final RemoteWebDriver driver, final Account account, final DateFilter dateFilter) {
        goToStart(driver);

        final WebDriverWait wait = new WebDriverWait(driver, TEN_SECONDS_DUR);
        wait.until(elementToBeClickable(By.xpath(String.format("//span[@class='iceOutTxt' and text()='%s']/../..//span[text()='Movimientos']/..", account.getRawNumber()))))
                .click();

        waitMillis(ONE_SECOND);

        wait.until(elementToBeClickable(By.xpath("//label[text()='Entre fechas']/../input")))
                .click();

        waitMillis(ONE_SECOND);

        boolean allFilterCriteriaFieldsHaveValue;

        WebElement dateField = wait.until(presenceOfElementLocated(By.xpath("//input[@id='formCriterios:calendarioDesde_cmb_dias']")));
        allFilterCriteriaFieldsHaveValue = ensureFieldHasValue(driver, dateField, "formCriterios:calendarioDesde_cmb_dias", dateFilter.getFromD());

        dateField = driver.findElement(By.xpath("//input[@id='formCriterios:calendarioDesde_cmb_mes']"));
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioDesde_cmb_mes", dateFilter.getFromM());

        dateField = driver.findElement(By.xpath("//input[@id='formCriterios:calendarioDesde_cmb_anyo']"));
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioDesde_cmb_anyo", dateFilter.getFromY());

        dateField = driver.findElement(By.xpath("//input[@id='formCriterios:calendarioHasta_cmb_dias']"));
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioHasta_cmb_dias", dateFilter.getToD());

        dateField = driver.findElement(By.xpath("//input[@id='formCriterios:calendarioHasta_cmb_mes']"));
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioHasta_cmb_mes", dateFilter.getToM());

        dateField = driver.findElement(By.xpath("//input[@id='formCriterios:calendarioHasta_cmb_anyo']"));
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioHasta_cmb_anyo", dateFilter.getToY());

        if (!allFilterCriteriaFieldsHaveValue) {
            throw new RuntimeException("Error while setting values of fields to filter account movements by date");
        }

        driver.findElement(By.id("formCriterios:mostrar")).click();

        waitMillis(ONE_SECOND);

        if (!driver.findElements(By.xpath("//span[text()='No disponemos datos para tu consulta. Modifica los criterios de busqueda.']")).isEmpty()) {
            log.warn("No movements for account!");
            return;
        }

        log.debug("Getting account movements with date filter {} ...", dateFilter);

        // TODO: sometimes table is not shown because of a page error, in this case movements recovery should be restarted
        wait.until(presenceOfElementLocated(By.xpath("//table[@id='formListado:dataContent']")));

        var exportToExcelButton = wait.until(presenceOfElementLocated(By.id("formListado:resourceExcel")));
        moveToElementAndClick(driver, exportToExcelButton);

        log.debug("Downloading... waiting for file to be downloaded ...");

        File downloadedFile = properties.getDownloadPath().resolve("movimientos.xls").toFile();

        new FluentWait<>(downloadedFile)
                .withTimeout(ofSeconds(10))
                .ignoring(Exception.class)
                .pollingEvery(ofSeconds(2))
                .until(file -> file.exists() && file.canWrite());

        waitMillis(ONE_SECOND);

        final String renamedFile = String.format("%s-%s.xls",
                mappings.entrySet().stream()
                        .filter(e -> account.getNumber().endsWith(e.getKey()))
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse("NOTFOUND"),
                dateFilter.getTo().format(DateTimeFormatter.ofPattern("yyyyMM")));

        log.debug("Downloaded file! Renaming from movimientos.xls to {}", renamedFile);

        try {
            Files.move(
                    downloadedFile.toPath(),
                    properties.getDownloadPath().resolve(renamedFile),
                    REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error while renaming downloaded movimientos.xls file to {}", renamedFile, e);
        }

        waitMillis(ONE_SECOND);
    }

    private boolean ensureFieldHasValue(final RemoteWebDriver driver, final WebElement element, final String elementId, final String value) {
        boolean fieldHasValue;
        int times = 0;

        String currentValue = (String) driver.executeScript(String.format("return document.getElementById('%s').value", elementId));

        while (!(fieldHasValue = StringUtils.equals(currentValue, value)) && times < 10) {
            log.debug("Value of field '{}' is '{}' and is not the expected one '{}'", elementId, currentValue, value);
            if (times > 0) {
                waitMillis(500);
            }
            clickAndSendKeys(element, value);
            currentValue = (String) driver.executeScript(String.format("return document.getElementById('%s').value", elementId));
            times++;
        }

        return fieldHasValue;
    }

    private void clickAndSendKeys(final WebElement element, final String text) {
        element.click();
        for (String character : text.split("")) {
            waitMillis(100);
            element.sendKeys(character);
        }
    }

    private void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    private boolean swithToTab(final RemoteWebDriver driver) {
        final String TAB_TITLE = "Kutxabank";
        boolean switchedToTab = false;

        final Set<String> tabs = driver.getWindowHandles();
        final Iterator<String> tabsIterator = tabs.iterator();

        log.debug("There are {} tabs in the browser, looking for the right one", tabs.size());

        while (tabsIterator.hasNext() && !switchedToTab) {
            final String tab = tabsIterator.next();

            driver.switchTo().window(tab);

            new FluentWait<>(driver.getTitle())
                    .withTimeout(ofSeconds(10))
                    .pollingEvery(ofMillis(500))
                    .until(StringUtils::isNotEmpty);

            final String currentTabTitle = driver.getTitle();

            switchedToTab = StringUtils.equals(TAB_TITLE, currentTabTitle);

            if (!switchedToTab) {
                log.debug("Current tab (title: '{}') is not the right one (expected title: '{}'), switching to the next one ...", currentTabTitle, TAB_TITLE);
                waitMillis(2000);
            } else {
                log.debug("Tab found!");
                driver.manage().window().maximize();
            }
        }

        if (!switchedToTab) {
            log.warn("Expected tab could not be found between all the browser tabs");
        }

        return switchedToTab;
    }

    private void moveToElementAndClick(final RemoteWebDriver driver, final WebElement element) {
        driver.executeScript("arguments[0].scrollIntoView(true);", element);
        waitMillis(ONE_SECOND);
        element.click();
    }

    private final Function<String, String> accountCleanup = (rawAccount) -> rawAccount
            .trim()
            .replaceAll("\\s", "");
}
