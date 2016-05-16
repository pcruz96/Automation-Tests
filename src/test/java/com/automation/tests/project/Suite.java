package com.automation.tests.project;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.automation.selenium.Driver;
import com.automation.tests.BaseTest;

public class Suite extends BaseTest {

	@Test(groups={"Acceptance"}, enabled=true)
	public void c1_tc1() {
		System.out.println("step 1a");
		Driver.getDriver().get("http://www.yahoo.com");
	}

	@Test(groups={"Other"}, enabled=false)
	public void c2_tc2() {
		System.out.println("step 1b");
		Driver.getDriver().get("http://www.amazon.com");
		Assert.assertTrue(false);
	}
	
	@Test(groups={"Acceptance"}, enabled=true)
	public void c3_tc3() {
		System.out.println("step 1c");
		Driver.getDriver().get("http://www.apple.com");
		Assert.assertTrue(false);
	}
	
	@Test(groups={"Other"}, enabled=true)
	public void c4_tc4() {
		System.out.println("step 1c");
		Driver.getDriver().get("http://www.nike.com");
		Assert.assertTrue(false);
	}
	
	@Test(groups={"Other"}, enabled=true)
	public void c5_tc5() {
		System.out.println("step 1c");
		Driver.getDriver().get("http://www.converse.com");
		Assert.assertTrue(false);
	}
}