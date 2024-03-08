package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;

public interface BrowserDriverProvider {
    boolean applies(final BrowserType browser);

    RemoteWebDriver getDriver();
}
