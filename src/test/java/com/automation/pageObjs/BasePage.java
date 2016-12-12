package com.automation.pageObjs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.automation.selenium.Driver;
import com.automation.selenium.SeleniumUtils;

public abstract class BasePage extends SeleniumUtils {
	
	public WebDriver dvr = Driver.getDriver();
	
    abstract public void isPageLoaded();
    
    public boolean inOrder(String order, String column, int child) {
    	    	
    	this.waitForElementVisibility(By.cssSelector("tbody > tr > td"));
    	this.clickElement(By.xpath("//*[contains(text(),'"+column+"')]/span[@class='select']"));
    	
    	if (order.equals("desc")) {
    		this.clickElement(By.xpath("//*[contains(text(),'"+column+"')]/span[@class='select']"));	
    	}
    	
    	this.threadSleep(5000);
		List<String> aryList=new ArrayList<String>();
		List<String> aryList2=new ArrayList<String>();

		for (int i = 1; i < 3; i++) {
			String value = this.getTxt(By.cssSelector("tbody > tr:nth-child("+i+") > td:nth-child(" + child + ")"));
			aryList.add(value);
			aryList2.add(value);
		}
		
		Collections.sort(aryList);		
		
		if (order.equals("desc")) {
			Collections.sort(aryList, Collections.reverseOrder());
		}
		
		logger.info(aryList.toString());
		logger.info(aryList2.toString());
		
		if (aryList.toString().equals(aryList2.toString())) {
			return true;
	    }
		return false;
	}
    
    public boolean tableInOrder(By obj) {
		this.waitForElementVisibility(obj);
		List<WebElement> elms = dvr.findElements(obj);
		int i = 0;
		List<String> aryList=new ArrayList<String>();
		List<String> aryList2=new ArrayList<String>();
		
		for (WebElement we : elms) {
			String txt = we.getText();
			if (StringUtils.isAlphanumeric(txt.substring(0, 1))) {
				aryList.add(txt);
				aryList2.add(txt);
				i++;
				if (i == 3) {
					break;
				}
			}
		}
		Collections.sort(aryList);	
		logger.info(aryList.toString());
		logger.info(aryList2.toString());
		
		if (aryList.toString().equals(aryList2.toString())) {
			return true;
	    }
		return false;
	}
}

