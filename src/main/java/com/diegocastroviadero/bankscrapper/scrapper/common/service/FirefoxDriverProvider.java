package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties.BrowserType;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirefoxDriverProvider implements BrowserDriverProvider {
    private final ScrappingProperties properties;

    @Override
    public boolean applies(BrowserType browser) {
        return browser == BrowserType.FIREFOX;
    }

    @Override
    public RemoteWebDriver getDriver() {
        final FirefoxProfile profile = new FirefoxProfile();

        final FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        options.addPreference("javascript.enabled", true);
        options.addPreference("browser.download.alwaysOpenPanel", false);

        final RemoteWebDriver driver = new RemoteWebDriver(properties.getSeleniumHub().getUrl(), options);

        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        return driver;
    }
}
