package com.automation.pageObjs;

import org.openqa.selenium.WebDriver;

import com.automation.selenium.Driver;
import com.automation.selenium.SeleniumUtils;

public abstract class BasePage extends SeleniumUtils {
	
	WebDriver dvr = Driver.getDriver();
	
    abstract public void isPageLoaded();
}

