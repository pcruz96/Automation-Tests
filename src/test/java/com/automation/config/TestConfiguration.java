package com.automation.config;

import com.typesafe.config.*;

public final class TestConfiguration {

    private static Config config = ConfigFactory.load();
    private static Config sauceLabsConfig = ConfigFactory.load();
    private static Config browserStackConfig = ConfigFactory.load();
    private static Config dbConfig = ConfigFactory.load();
    private static Config jiraConfig = ConfigFactory.load();
    private static Config testRailConfig = ConfigFactory.load();

    public static Config getConfig() {
        return config;
    }
    
    public static Config getSauceLabsConfig() {
        return sauceLabsConfig;
    }
    
    public static Config getBrowserStackConfig() {
        return browserStackConfig;
    }
    
    public static Config getDbConfig() {
        return dbConfig;
    }
    
    public static Config getJiraConfig() {
        return jiraConfig;
    }
    
    public static Config getTestRailConfig() {
        return testRailConfig;
    }
    
    public static void setConfig(String confBaseName) {
        config = ConfigFactory.load("config/env.conf");
        sauceLabsConfig = ConfigFactory.load("config/sauceLabs.conf");
        browserStackConfig = ConfigFactory.load("config/browserStack.conf");
        dbConfig = ConfigFactory.load("config/db.conf");
        jiraConfig = ConfigFactory.load("config/jira.conf");
        testRailConfig = ConfigFactory.load("config/testRail.conf");
    }
}
