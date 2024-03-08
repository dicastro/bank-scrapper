package com.diegocastroviadero.bankscrapper.scrapper.common.service;

import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties.BrowserType;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DriverProvider {
    private final ScrappingProperties properties;
    private final List<BrowserDriverProvider> driverProviders;
    public RemoteWebDriver getDriver() {
        return driverProviders.stream()
                .filter(bdp -> bdp.applies(properties.getBrowser()))
                .findFirst()
                .map(BrowserDriverProvider::getDriver)
                .orElseThrow();
    }
}
