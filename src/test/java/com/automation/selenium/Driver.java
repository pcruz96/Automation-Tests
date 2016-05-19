package com.automation.selenium;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.automation.config.TestConfiguration;

public class Driver {

    private static ThreadLocal<WebDriver> threadDvr = new ThreadLocal<WebDriver>();

    public static WebDriver createDriver(String name, String env, Platform platform, String browser, String version, String deviceName, String deviceOrientation, Boolean sauceLabs, Method method) throws MalformedURLException {

        DesiredCapabilities cap = null;
        name = name != null ? name : "";
        platform = platform != null ? platform : Platform.ANY;
        browser = browser != null ? browser : "firefox";
        version = version != null ? version : "";
        //mobile parameters
        deviceName = deviceName != null ? deviceName : "";
        deviceOrientation = deviceOrientation != null ? deviceOrientation : "portrait";
        sauceLabs = sauceLabs != null ? sauceLabs : false;

        if (browser.equalsIgnoreCase("firefox")) {

            cap = DesiredCapabilities.firefox();            
            cap.setPlatform(platform);
            cap.setBrowserName(DesiredCapabilities.firefox().getBrowserName());
            cap.setVersion(version);

        } else if (browser.equalsIgnoreCase("ie")) {

            cap = DesiredCapabilities.internetExplorer();
            cap.setPlatform(platform);
            cap.setBrowserName(DesiredCapabilities.internetExplorer().getBrowserName());
            cap.setVersion(version);            
            cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
            cap.setCapability("ignoreProtectedModeSettings", true);
            cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            cap.setJavascriptEnabled(true);
            cap.setCapability("requireWindowFocus", true);
            cap.setCapability("enablePersistentHover", false);
            System.setProperty("webdriver.ie.driver", System.getenv("HOME") + "/Selenium/IEDriverServer.exe");

        } else if (name.equalsIgnoreCase("android")) {

            cap = DesiredCapabilities.android();
            cap.setPlatform(platform);
            //cap.setVersion(version);
            cap.setCapability("deviceName", deviceName);
            cap.setCapability("device-orientation", deviceOrientation);

        } else if (browser.equalsIgnoreCase("chrome")) {
            if (!sauceLabs) {
                System.setProperty("webdriver.chrome.driver", "selenium/chromedriver");
            }
            cap = DesiredCapabilities.chrome();
            cap.setPlatform(platform);
            cap.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
            cap.setVersion(version);

        } else if (browser.equalsIgnoreCase("iPad")) {
        	
            cap = DesiredCapabilities.ipad();
            cap.setPlatform(platform);
            cap.setBrowserName(DesiredCapabilities.ipad().getBrowserName());
            cap.setVersion(version);
            cap.setCapability("device-orientation", deviceOrientation);
            
        } else if (browser.equalsIgnoreCase("phantomjs")) {
        	
            cap = DesiredCapabilities.phantomjs();
        }
        
        cap.setCapability("name", method.getName());
        cap.setCapability("tags", env);
        cap.setCapability("build", System.getenv("BUILD_TAG"));
		//cap.setCapability("commandTimeout", 120);
        
        if (sauceLabs) {        	
        	String apiKey = TestConfiguration.getSauceLabsConfig().getString("APIKEY");
        	threadDvr.set(new RemoteWebDriver(new URL("http://"+apiKey+"@ondemand.saucelabs.com:80/wd/hub"), cap));
        } else {            
        	try {        		
        		threadDvr.set(new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap));
        	} catch (Exception e) {
        		threadDvr.set(new FirefoxDriver());
        	}
        }
        return threadDvr.get();
    }

    public static WebDriver getDriver() {
        return threadDvr.get();
    }
}
