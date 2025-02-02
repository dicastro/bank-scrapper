package com.diegocastroviadero.bankscrapper.configuration;

import com.diegocastroviadero.bankscrapper.model.Bank;
import com.diegocastroviadero.bankscrapper.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.diegocastroviadero.bankscrapper.configuration.ScrappingProperties.SCRAPPING_CONFIG_PREFIX;
import static java.time.Duration.ofSeconds;

@Getter
@Setter
@ConfigurationProperties(prefix = SCRAPPING_CONFIG_PREFIX)
public class ScrappingProperties {
    public static final String SCRAPPING_CONFIG_PREFIX = "bankscrapper";

    private BrowserType browser;
    private SeleniumHubProperties seleniumHub;
    private TesseractProperties tesseract;
    private Path downloadPath;
    private Map<User, List<Bank>> userBanks;
    private BanksProperties banks;
    private Waits waits;

    public static enum BrowserType {
        CHROME, FIREFOX;
    }

    @Getter
    @Setter
    public static class SeleniumHubProperties {
        private URL url;
    }

    @Getter
    @Setter
    public static class TesseractProperties {
        private Path commandPath;
        private Path basePath;
        private Path dataPath;
        private Path configPath;
        private String lang;
        private Integer pageSegmentationMode;
        private Integer ocrEngineMode;
        private Integer dpi;
    }

    @Getter
    @Setter
    public static class BanksProperties {
        private KbProperties kb;
    }

    @Getter
    @Setter
    public static class KbProperties {
        private KeyboardCacheProperties keyboardCache;
    }

    @Getter
    @Setter
    public static class KeyboardCacheProperties {
        private Path basePath;
        private Path cache;
        private Path tmp;
    }

    @Getter
    @Setter
    public static class Waits {
        private Duration defaultWait = ofSeconds(10);
        private Duration goToStartWait;

        public Duration getGoToStartWaitOrDefault() {
            return Optional.ofNullable(goToStartWait).orElse(defaultWait);
        }
    }
}
