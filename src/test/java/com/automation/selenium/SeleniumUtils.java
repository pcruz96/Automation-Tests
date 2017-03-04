package com.automation.selenium;

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

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

import com.automation.tests.BaseTest;
import com.automation.utils.Log4J;
import com.automation.utils.StackTraceToString;

public class SeleniumUtils extends Log4J {

	WebDriver driver = Driver.getDriver();
	public int MAX_WAIT = 20;
	boolean printLogs;

	public SeleniumUtils() {		
		printLogs = true;
	}

	public SeleniumUtils(boolean printLog) {		
		printLogs = printLog;
	}

	public Wait<WebDriver> fluentWait() {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
				.withTimeout(MAX_WAIT, TimeUnit.SECONDS)
				.pollingEvery(1, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class);
		return wait;
	}

	public boolean waitForElementPresent(By locator) {
		this.waitForPageLoaded();
		try {			
			this.fluentWait().until(ExpectedConditions.presenceOfElementLocated(locator));					
			return true;
		} catch (Exception e) {		
			logger.error(BaseTest.getMethodName() + " - " + locator.toString() + " is not present.\n");			
		} 
		return false;
	}

	public boolean waitForElementPresentInFrame(By frame, By locator) {
		this.threadSleep(5000);
		try {
			this.switchToFrame(frame);
			this.fluentWait().until(ExpectedConditions.presenceOfElementLocated(locator));
			return true;
		} catch (Exception e) {
			logger.error(BaseTest.getMethodName() + " - " + locator.toString() + " is not present in frame.\n");			
		}
		return false;
	}

	public boolean waitForTextPresent(String text) {
		this.waitForPageLoaded();
		try {
			this.fluentWait().until(ExpectedConditions
					.textToBePresentInElement(
							driver.findElement(By.tagName("body")), text));
			return true;
		} catch (Exception e) {
			logger.error(BaseTest.getMethodName() + " - " + text + " is not present.\n");			
		}
		return false;
	}

	public boolean waitForElementClickable(By locator) {
		try {
			this.fluentWait().until(ExpectedConditions.elementToBeClickable(locator));
			return true;
		} catch (TimeoutException e) {
			logger.error(BaseTest.getMethodName() + " - " + locator.toString() + " is not clickable.\n");			
		}
		return false;
	}

	public boolean waitForElementVisibility(By locator) {
		this.waitForPageLoaded();
		try {
			this.fluentWait().until(ExpectedConditions
					.visibilityOfElementLocated(locator));
			return true;
		} catch (Exception e) {
			logger.error(BaseTest.getMethodName() + " - " + locator.toString() + " is not visible.\n");			
		}
		return false;
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

	public String getSelectedOptionText(String index) {		
		return driver.findElement(By.cssSelector("option[value='"+index+"']")).getText();
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

	public void waitUntilElementVisible(int sec, By locator) {
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
		try {
			this.waitForElementVisibility(locator);
			driver.findElement(locator).click();										
		} catch (Exception e) {
			clickHiddenElement(locator);
		}
	}

	public void clickHiddenElement(By locator) {
		this.waitForElementVisibility(locator);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].style.visibility='visible';", driver.findElement(locator));
		executor.executeScript("arguments[0].click();", driver.findElement(locator));		
	}
	
	public void javascriptClick(By locator) {
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", driver.findElement(locator));
	}

	public void selectOption(By locator, String option) {
		waitForElementVisibility(locator);
		Select list = new Select(driver.findElement(locator));
		list.selectByVisibleText(option);
		this.waitForPageLoaded();
	}

	public void selectOptionIndex(By locator, int index) {
		this.waitForElementVisibility(locator);
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

	public void threadSleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean dragAndDrop(By src, By tgt, By expectElement) {		
		try {
			WebElement we = driver.findElement(src);
			WebElement target = driver.findElement(tgt);
			(new Actions(driver)).dragAndDrop(we, target).perform();
			waitForElementVisibility(expectElement);
			this.threadSleep(10000);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getElementAttribute(By locator, String attribute) {
		waitForElementVisibility(locator);
		String elementAttribute = driver.findElement(locator).getAttribute(attribute);				
		return elementAttribute;
	}

	public boolean waitForPageLoaded() {

		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript(
						"return document.readyState").equals("complete");
			}
		};

		try {
			fluentWait().until(expectation);
			return true;
		} catch (Throwable error) {
			return false;
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
		String bodyTxt = getBodyTxt().toLowerCase();
		return bodyTxt.contains(txt.toLowerCase());
	}

	public String dateFormat(String format, int days) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar cal = Calendar.getInstance();
		// can add + or - prefix to days 
		cal.add(Calendar.DATE, days);    
		String modifiedDate = dateFormat.format(cal.getTime());		
		return modifiedDate;
	}
	
	public String weekdayDateFormat(String format, int days) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar cal = Calendar.getInstance();
		// can add + or - prefix to days     
		cal.setTime(cal.getTime());
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 7 || dayOfWeek == 1) {
			cal.add(Calendar.DATE, days + 2);    				
		} else {
			cal.add(Calendar.DATE, days);    
		}
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

	public String getRandomAlphaString(int length) {
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}

	public String getRandomAlphaNumString(int length) {
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ0123456789";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}

	public String getRandomNumSpecialCharString(int length) {
		final String characters = "0123456789!@#$%^&*()_+<,>.?/:;{[}]|";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}

	public String getRandomAlphaSpecialCharString(int length) {
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ!@#$%^&*()_+,.?/:;{[}]|";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}

	public String getRandomSpecialCharString(int length) {
		final String characters = "!@#$%^&*()_+,.?/:;{[}]|";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			Random rand = new Random();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}

	public String getRandomNumber(int length) {
		final String characters = "0123456789";
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
		this.threadSleep(3000);
		this.waitForElementVisibility(locator);
		return driver.findElement(locator).getText();
	}

	public boolean isSelected(By locator) {
		this.waitForElementVisibility(locator);
		return driver.findElement(locator).isSelected();
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
		fluentWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
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
		for (int i = 0; i < 10; i++) {
			try {			
				this.threadSleep(10000);
				driver.switchTo().alert().accept();
				break;
			} catch (Exception e) {
			}									
		}
	}	
	
	public void acceptAlert(By obj) {
		try {			
			this.clickElement(obj);
			this.threadSleep(10000);
			driver.switchTo().alert().accept();
		} catch (Exception e) {
		}
	}

	public void dismissAlert() {
		try {			
			this.threadSleep(2000);
			driver.switchTo().alert().dismiss();
		} catch (Exception e) {}		
	}
	
	public void dismissAlert(By obj) {
		try {			
			this.clickElement(obj);
			this.threadSleep(2000);
			driver.switchTo().alert().dismiss();
		} catch (Exception e) {}		
	}

	public String getCssValueByXpath(By locator, String style){
		return driver.findElement(locator).getCssValue(style);
	}

	public void mouseHover (By locator) {
		Actions action = new Actions(driver);
		WebElement we = driver.findElement(locator);
		action.moveToElement(we).build().perform();
	}	

	public int getTableRowCount() {
		return driver.findElements(By.xpath("//tbody/tr")).size();
	}

	public void deleteCookies(){
		driver.manage().deleteAllCookies();
	}

	public String getAlertTxt() {
		if (BaseTest.browser.equals("safari")) {
			this.threadSleep(10000);
			return driver.switchTo().alert().getText();
		} else {
			for (int i = 0; i < 10; i++) {
				try {			
					this.threadSleep(10000);
					String alertTxt = driver.switchTo().alert().getText();
					return alertTxt;
				} catch (Exception e) {
				}									
			}
		}
		return null;
	}

	public String removeSpecialChars(String str) {
		return str.replaceAll("[^a-zA-Z0-9]+","");
	}

	public String getExceptionAlertTxt(By element) {
		String alertTxt;
		try {
			try {
				this.clickElement(element);
			} catch (Exception e) {
				this.clickHiddenElement(element);
			}
			alertTxt = this.getAlertTxt();
			this.acceptAlert();
		} catch (Exception e) {
			StackTraceToString st = new StackTraceToString();
			alertTxt = st.getStackTrace(e);
		}
		return alertTxt;
	}

	public String getModalTxt(By element) {
		String alertTxt = null;
		try {
			this.clickHiddenElement(element);
		} catch (Exception e) {
			StackTraceToString st = new StackTraceToString();
			alertTxt = st.getStackTrace(e);
		}
		return alertTxt;
	}

	public void switchToActiveElement() {
		driver.switchTo().activeElement();	
	}

	public int getDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek;
	}

	public void clickText(String txt, boolean isHidden) {
		this.waitForTextPresent(txt);

		if (isHidden) {
			this.clickHiddenElement(By.xpath("//*[contains(text(),'"+txt+"')]"));	
		} else {
			this.clickElement(By.xpath("//*[contains(text(),'"+txt+"')]"));	
		}						
	}

	public int getElementsCount(By element) {
		this.waitForElementVisibility(element);
		return driver.findElements(element).size();
	}

	public int getCurrentMonthInt() {
		java.util.Date date= new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MONTH);	
	}	

	public boolean isAlertPresent() 
	{ 
		try{ 
			driver.switchTo().alert(); 
			return true; 
		}catch (NoAlertPresentException Ex){ 
			return false; 
		}
	} 

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		java.util.Date date= new Date();
		String currentDate = dateFormat.format(date);
		return currentDate; 
	}

	public static String getCurrentDateOffset(int offset) {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, offset);
		return dateFormat.format(calendar.getTime()).toString();
	}
	
	public String getDateFormat(String format) {
		Calendar cal = Calendar.getInstance();
		return new SimpleDateFormat(format).format(cal.getTime());
	}
	
	public void multiselect(int start, By obj, int get) {		
		for (int i = start; i < (get + 2); i++) {
			List<WebElement> items = driver.findElements(obj);
			Actions actions = new Actions(driver);
			try {
				actions.click(items.get(i)).keyDown(Keys.COMMAND).click(items.get(i+1)).keyUp(Keys.COMMAND).build().perform();
			} catch (Exception e) {}
		}
	}
	
	public void scrollIntoView(By obj) {
		this.waitForPageLoaded();
		WebElement element = driver.findElement(obj);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].scrollIntoView(true);", element);
	}
	
	public void fileUpload(By obj, String file) {
		WebElement upload = driver.findElement(obj);
		File f = new File(file);
		String file2 = f.getAbsolutePath();
        upload.sendKeys(file2);
	}
	
	public void dragAndDropAction(By src, By tgt) {
		this.waitForElementVisibility(src);
		Actions builder = new Actions(driver);
		Action dragAndDrop = builder.clickAndHold(driver.findElement(src))
		   .moveToElement(driver.findElement(tgt))
		   .release(driver.findElement(tgt))
		   .build();
		dragAndDrop.perform();
	}
	
	public boolean isTimeStampValid(String format, String inputString) {
		SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		try {
			sdf.parse(inputString);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	
	public String getCssValue(By obj, String cssField) {
		this.waitForElementVisibility(obj);
		return driver.findElement(obj).getCssValue(cssField);
	}
}
