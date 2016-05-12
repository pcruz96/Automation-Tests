package com.automation.config;

import com.typesafe.config.*;

public final class TestConfiguration {

    private static Config config = ConfigFactory.load();
    private static Config sauceLabsConfig = ConfigFactory.load();
    private static Config dbConfig = ConfigFactory.load();

    public static Config getConfig() {
        return config;
    }
    
    public static Config getSauceLabsConfig() {
        return sauceLabsConfig;
    }
    
    public static Config getDbConfig() {
        return dbConfig;
    }
    
    public static void setConfig(String confBaseName) {
        config = ConfigFactory.load("config/" + confBaseName);
        sauceLabsConfig = ConfigFactory.load("config/sauceLabs.conf");
        dbConfig = ConfigFactory.load("config/db.conf");
    }
}
