package com.automation.tests.project;

import org.testng.annotations.Test;

import com.automation.selenium.Driver;
import com.automation.tests.BaseTest;

public class Suite extends BaseTest {

	@Test(groups={"smoke"}, enabled=true)
	public void c1_tc1() {
		System.out.println("step 1a");
		Driver.getDriver().get("http://www.yahoo.com");
	}

	@Test(groups={"smoke"}, enabled=false)
	public void c2_tc2() {
		System.out.println("step 1b");
		Driver.getDriver().get("http://www.amazon.com");
	}
}