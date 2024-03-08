package com.diegocastroviadero.bankscrapper.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {ScrappingProperties.class})
public class BankScrapperConfiguration {
}
