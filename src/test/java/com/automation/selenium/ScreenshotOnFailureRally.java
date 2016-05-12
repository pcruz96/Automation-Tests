package com.automation.selenium;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

import com.automation.exceptionHandling.CustomException;
import com.automation.tests.BaseTestRally;
import com.automation.utils.Rally;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ScreenshotOnFailureRally extends CustomException {
	
	StringWriter errors = new StringWriter();
	
	public void takeScreenShotOnFailure(ITestResult testResult, WebDriver driver, Method method, String sauceLabsJobIdLink) throws IOException {			

		Rally rally = new Rally();
		String caseId = rally.getTestCaseId(method.getName());					
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String error = "";
		try {
			error = getStackTrace(testResult, method);
			logger.error(BaseTestRally.getMethodName() + " - " + error + "\n");				
		} catch (Exception e) {
			logger.error("EXCEPTION: getStackTrace(testResult, method);");
			e.printStackTrace(new PrintWriter(errors));				
			logger.error(errors);
		}
		if (!BaseTestRally.sauceLabs) {
			try {
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				String fileName = "failed." + caseId + "." + error + "." + timeStamp + ".png";
				if (fileName.length() > 256) {
					fileName = "failed." + caseId + "." + error + ".png";
				}
				FileUtils.copyFile(scrFile, new File("test-output/screenshots/" + fileName));
			} catch (Exception e) {
				logger.error("EXCEPTION: FileUtils.copyFile(scrFile, new File(\"test-output/screenshots/\" + fileName));");
			}
		}			
		if (BaseTestRally.updRally) {
			try {				 
				String steps = rally.getAutomatedTestCaseSteps(BaseTestRally.suiteName, method.getName());
				rally.createTestResult(BaseTestRally.getBuildName(), rally.getTestCaseId(method.getName()), "Fail", error + "<br/><br/>" + steps + "<br/>" + rally.getLink(sauceLabsJobIdLink) + "<br/><br/>" + rally.getLink(BaseTestRally.getBuildUrl()));
			} catch (Exception e) {
				logger.error(errors);
			}
		}
	}
}

