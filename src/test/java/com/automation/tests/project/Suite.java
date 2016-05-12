package com.automation.tests.project;

import org.testng.annotations.Test;

import com.automation.tests.BaseTest;

public class Suite extends BaseTest {

	@Test(groups={"smoke"}, enabled=true)
	public void c1_tc1() {
		System.out.println("step 1a");
	}

	@Test(groups={"smoke"}, enabled=false)
	public void c2_tc2() {
		System.out.println("step 1b");
	}
}