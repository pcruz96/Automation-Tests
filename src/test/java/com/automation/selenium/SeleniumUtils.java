package com.automation.selenium;

import com.automation.exceptionHandling.CustomException;
import com.automation.utils.Log4J;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.Reporter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeleniumUtils extends Log4J {

	WebDriver driver = Driver.getDriver();
	public int MAX_WAIT = 30;
	boolean printLogs;
	
	public SeleniumUtils() {		
		printLogs = true;
	}
	
	public SeleniumUtils(boolean printLog) {		
		printLogs = printLog;
	}

	public void waitForElementPresent(By locator) {
		boolean success = false;
		try {
			Wait<WebDriver> wait = this.fluentWait();
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));					
			success = true;
		} catch (Exception e) {}		
		Assert.assertTrue(success, locator.toString() + " is not present.");
	}
	
	public void waitForElementPresentInFrame(By frame, By locator) {
		this.threadSleep(5000);
		boolean success = false;
		try {
			this.switchToFrame(frame);
			Wait<WebDriver> wait = this.fluentWait();
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			success = true;
		} catch (Exception e) {}
		Assert.assertTrue(success, locator.toString() + " is not present in frame.");
	}

	public void waitForTextPresent(String text) {
		boolean success = false;
		try {
			Wait<WebDriver> wait = this.fluentWait();
			success = wait.until(ExpectedConditions
					.textToBePresentInElement(
							driver.findElement(By.tagName("body")), text));
		} catch (Exception e) {}
		Assert.assertTrue(success, text + " is not present.");
	}

	public void waitForElementClickable(By locator) {
		boolean success = false;
		try {
			Wait<WebDriver> wait = this.fluentWait();
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			success = true;
		} catch (TimeoutException e) {}
		Assert.assertTrue(success, locator.toString() + " is not clickable.");
	}

	public void waitForElementVisibility(By locator) {
		boolean success = false;
		try {
			Wait<WebDriver> wait = this.fluentWait();
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(locator));
			success = true;
		} catch (Exception e) {}
		Assert.assertTrue(success, locator.toString() + " is not visible.");
	}

	public WebDriver switchToWindow(String regex) throws NoSuchWindowException {
		boolean windowfound = false;
		Set<String> windows = driver.getWindowHandles();

		for (String window : windows) {
			driver.switchTo().window(window);
			logger.info(String.format("#switchToWindow() : title=%s ; url=%s",
					driver.getTitle(), driver.getCurrentUrl()));

			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(driver.getTitle());

			//if (m.find()) {
			if (driver.getTitle().matches(regex)) {
				windowfound = true;
				break;
			} else {
				m = p.matcher(driver.getCurrentUrl());
				if (m.find()) {
					windowfound = true;
					break;
				}
			}
		}
		if (windowfound)
			return driver;
		else {
			Reporter.log("Could not switch to window with title / url: "
					+ regex);
			throw new NoSuchWindowException(
					"Could not switch to window with title / url: " + regex);
		}
	}

	// FluentWait instance defines the maximum amount of time to wait for a
	// condition,
	// as well as the frequency with which to check the condition
	public Wait<WebDriver> fluentWait() {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
				.withTimeout(MAX_WAIT, TimeUnit.SECONDS)
				.pollingEvery(1, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class);
		return wait;
	}

	public String getOptionsText(By locator) {
		Select list = new Select(driver.findElement(locator));
		List<WebElement> options = list.getOptions();
		StringBuilder optionsText = new StringBuilder();
		for (WebElement we : options) {
			optionsText.append(we.getText() + ", ");
		}
		logger.info(optionsText.toString());
		return optionsText.toString().trim();
	}

	public void waitUntilElementVisible(Integer sec, By locator) {
		WebDriverWait wait = new WebDriverWait(driver, sec);
		wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public void fillTxt(By locator, String txt) {
		this.waitForElementVisibility(locator);
		try {
			driver.findElement(locator).clear();
		} catch (Exception e) {}					
		driver.findElement(locator).sendKeys(txt);
	}

	public void clickElement(By locator) {
		this.waitForElementVisibility(locator);
		this.waitForElementClickable(locator);
		try {
			driver.findElement(locator).click();
		} catch (Exception e) {
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", driver.findElement(locator));
		}
		this.waitForPageLoaded();
	}
	
	public void clickHiddenElement(By locator) {		
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].style.visibility='visible';", driver.findElement(locator));
		executor.executeScript("arguments[0].click();", driver.findElement(locator));		
	}

	public void selectOption(By locator, String option) {
		waitForElementVisibility(locator);
		Select list = new Select(driver.findElement(locator));
		list.selectByVisibleText(option);
		this.waitForPageLoaded();
	}
	
	public void selectOptionIndex(By locator, int index) {
		waitForElementVisibility(locator);
		Select list = new Select(driver.findElement(locator));
		list.selectByIndex(index);
		this.waitForPageLoaded();
	}

	// Get the row count from the htmltable.
	public int getRowCount(By locator) {
		try {
			WebElement table = driver.findElement(locator);
			List<WebElement> rows = table.findElements(By.tagName("tr"));
			return rows.size();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return -1;
		}
	}
	
	public List<WebElement> getColmn(By locator) {
			
		try {
			List<WebElement> cols = driver.findElements(locator);
			return cols;
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
		

	// Scroll down using java script.
	public void scrollDown() {
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("javascript:window:scrollBy(250,450)");
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}

	public String getBodyTxt() {
		String bodyText = null;
		By locator = By.tagName("body");
		waitForElementVisibility(locator);
		bodyText = driver.findElement(locator).getText();
		return bodyText;
	}

	public void threadSleep(Integer milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dragAndDrop(By src, By tgt, By expectElement) {
		boolean success = false;
		try {
			WebElement we = driver.findElement(src);
			WebElement target = driver.findElement(tgt);
			(new Actions(driver)).dragAndDrop(we, target).perform();
			waitForElementVisibility(expectElement);
			success = true;
		} catch (Exception e) {}
		if (!success) {
			CustomException ce = new CustomException();
			ce.throwException("failed to drag and drop the source element, "
					+ src.toString() + ", to the target element, "
					+ tgt.toString());
		}
	}

	public String getElementAttribute(By locator, String attribute) {
		waitForElementVisibility(locator);
		String elementAttribute = driver.findElement(locator).getAttribute(
				attribute);
		return elementAttribute;
	}

	public void waitForPageLoaded() {

		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript(
						"return document.readyState").equals("complete");
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
				.withTimeout(MAX_WAIT, TimeUnit.SECONDS)
				.pollingEvery(1, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class);
		try {
			wait.until(expectation);
		} catch (Throwable error) {

		}
	}
	
	public String getElementActualValue(By element) {		
		return driver.findElement(element).getAttribute("value");
	}
	
	public void takeScreenshot(String name) {
		File scrFile = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.FILE);					
		try {
			FileUtils.copyFile(scrFile, new File("test-output/screenshots/"+name+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public boolean bodyTxtContains(String txt) {
		this.waitForTextPresent(txt);
		String bodyTxt = getBodyTxt();
		return bodyTxt.contains(txt);
	}
	
	public String dateFormat(String format, Integer days) {
		DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        // can add + or - prefix to days 
        cal.add(Calendar.DATE, days);    
        String modifiedDate = dateFormat.format(cal.getTime());
        return modifiedDate;
	}
	
	public Date getDate(String format, String date){
		try{
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			return formatter.parse(date);
		}
		catch(ParseException e){
			System.out.println(e);
		}
		return null;
	}
	
	public String getRandomString(int length) {
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_+";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}
	
	public String getStringBeforeSingleQuote(String txt) {
		String[] s = txt.split("'");
		return s[0];
	}
	
	public String getTxt(By locator) {
		this.waitForElementVisibility(locator);
		return driver.findElement(locator).getText();
	}
	
	public boolean isSelected(By locator) {
		return driver.findElement(locator).isSelected();
	}
	
	public void clickObjTxt2AfterTxt1(String txt1, String txt2) {
		driver.findElement(By.xpath("//*[contains(text(), '"+txt1+"')]/following::*[contains(text(), '"+txt2+"')]")).click();	
	}
	
	public boolean stringContainsNumbers(String str) {
		return str.matches(".*\\d.*");
	}
	
	public boolean stringContainsSpecialChar(String str) {
		String patternToMatch = "[\\\\!\"#$%&()*+,./:;<=>?@\\[\\]^_{|}~]+";
		Pattern p = Pattern.compile(patternToMatch);
		Matcher m = p.matcher(str);			
		boolean characterFound = m.find();
		return characterFound;
	}
	
	public void switchToFrame(By locator) {
		try {
			WebElement we = driver.findElement(locator);		
			driver.switchTo().frame(we);
			this.threadSleep(3000);
			this.waitForPageLoaded();
		} catch (Exception e) {}
	}
	
	public int objCount(By locator) {
		return driver.findElements(locator).size();		
	}
	
	public String getMonthName(int month){
	    String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	    return monthNames[month];
	}

	public void sendkeys(By locator, Keys key) {
		try {
			driver.findElement(locator).sendKeys(key);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public boolean objDisplayed(By locator) {
		this.waitForElementVisibility(locator);
		return driver.findElement(locator).isDisplayed();	
	}
	
	public boolean isEnabled(By locator) {
		this.waitForElementVisibility(locator);
		return driver.findElement(locator).isEnabled();	
	}
	
	public void acceptAlert() {
		this.threadSleep(2000);
		driver.switchTo().alert().accept();
	}	
	
	public String getCssValueByXpath(By locator, String style){
		return driver.findElement(locator).getCssValue(style);
	}
	
	public void mouseHover (By locator) {
		Actions action = new Actions(driver);
		WebElement we = driver.findElement(locator);
		action.moveToElement(we).build().perform();
	}	
	
	public Integer getTableRowCount() {
		return driver.findElements(By.xpath("//tbody/tr")).size();
	}
	
	public void deleteCookies(){
		driver.manage().deleteAllCookies();
	}
	
	public void pressKeys(By locator, Keys key){
		driver.findElement(locator).sendKeys(key);
	}
	
	public String getAlertTxt() {
		this.threadSleep(2000);
		return driver.switchTo().alert().getText();	
	}	
}
