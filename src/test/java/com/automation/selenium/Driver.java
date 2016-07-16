package com.automation.selenium;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.automation.config.TestConfiguration;
import com.automation.tests.BaseTest;

public class Driver {

    private static ThreadLocal<WebDriver> threadDvr = new ThreadLocal<WebDriver>();

    public static WebDriver createDriver(String name, String env, String os, String os_version, String browser, String version, String deviceName, String deviceOrientation, Boolean cloudTest, Method method) throws MalformedURLException {

        DesiredCapabilities cap = null;
        String buildTag = System.getenv("BUILD_TAG"); 
        name = name != null ? name : "";
        browser = browser != null ? browser : "firefox";
        version = version != null ? version : "";
        //mobile parameters
        deviceName = deviceName != null ? deviceName : "";
        deviceOrientation = deviceOrientation != null ? deviceOrientation : "portrait";
        cloudTest = cloudTest != null ? cloudTest : false;
        buildTag = buildTag != null ? buildTag : "";

        if (browser.equalsIgnoreCase("firefox")) {

            cap = DesiredCapabilities.firefox();            
            cap.setBrowserName(DesiredCapabilities.firefox().getBrowserName());
            cap.setVersion(version);

        } else if (browser.equalsIgnoreCase("ie")) {

            cap = DesiredCapabilities.internetExplorer();
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
            //cap.setVersion(version);
            cap.setCapability("deviceName", deviceName);
            cap.setCapability("device-orientation", deviceOrientation);

        } else if (browser.equalsIgnoreCase("chrome")) {
            
            cap = DesiredCapabilities.chrome();
            cap.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
            cap.setVersion(version);
            
        } else if (browser.equalsIgnoreCase("safari")) {
            
            cap = DesiredCapabilities.safari();
            cap.setBrowserName(DesiredCapabilities.safari().getBrowserName());
            cap.setVersion(version);

        } else if (browser.equalsIgnoreCase("iPad")) {
        	
            cap = DesiredCapabilities.ipad();
            cap.setBrowserName(DesiredCapabilities.ipad().getBrowserName());
            cap.setVersion(version);
            cap.setCapability("device-orientation", deviceOrientation);
            
        } else if (browser.equalsIgnoreCase("phantomjs")) {
        	
            cap = DesiredCapabilities.phantomjs();
        }
        
        cap.setCapability("name", method.getName());
        cap.setCapability("tags", env);
        cap.setCapability("build", buildTag.toLowerCase());
        cap.setCapability("screenResolution", "1376x1032");
        cap.setCapability("acceptSslCerts", true);        
		//cap.setCapability("idleTimeout", 240);
        
        if (cloudTest) {        	
        	
        	if (BaseTest.cloudTestProvider.equals("sauceLabs")) {
	        
        		cap.setCapability("platform", os);
        		String apiKey = TestConfiguration.getSauceLabsConfig().getString("APIKEY");
	        	threadDvr.set(new RemoteWebDriver(new URL("http://"+apiKey+"@ondemand.saucelabs.com:80/wd/hub"), cap));
        	
        	} else if (BaseTest.cloudTestProvider.equals("browserStack")) {

        		cap.setCapability("os", os);
                cap.setCapability("os_version", os_version);
                cap.setCapability("browserstack.debug", "true");
        		String apiKey = TestConfiguration.getBrowserStackConfig().getString("APIKEY");
            	threadDvr.set(new RemoteWebDriver(new URL("http://"+apiKey+"@hub-cloud.browserstack.com/wd/hub"), cap));
        	}
        } else {            
        	try {        		
        		//threadDvr.set(new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap));
        		threadDvr.set(new FirefoxDriver());
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
