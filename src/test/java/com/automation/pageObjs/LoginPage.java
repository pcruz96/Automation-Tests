
package com.automation.pageObjs;

import com.automation.config.TestConfiguration;

import org.openqa.selenium.By;

public class LoginPage extends BasePage {
	
	By txtUsername = By.xpath("//input[@name='userName']");
	By txtPassword = By.xpath("//input[@name='password']");
	By btnLogin = By.id("loginButton");
	
	public LoginPage() {
		dvr.get(TestConfiguration.getConfig().getString("login.url"));
		isPageLoaded();
	}
		
	public void isPageLoaded() {
		//waitForElementPresent(btnLogin);
	}
		
	public void login() {
		/*		
		this.fillTxt(txtUsername, TestConfiguration.getConfig().getString("login.username"));
		this.fillTxt(txtPassword, TestConfiguration.getConfig().getString("login.password"));
		this.clickElement(btnLogin);
		*/		
	}	
}
