package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties.BrowserType;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChromeDriverProvider implements BrowserDriverProvider {
    private final ScrappingProperties properties;

    @Override
    public boolean applies(BrowserType browser) {
        return browser == BrowserType.CHROME;
    }

    @Override
    public RemoteWebDriver getDriver() {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--enable-javascript");

        final RemoteWebDriver driver = new RemoteWebDriver(properties.getSeleniumHub().getUrl(), options);

        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        return driver;
    }
}
