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
import com.automation.tests.BaseTest;
import com.gurock.testrail.TestRailUtilities;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ScreenshotOnFailure extends CustomException {
	
	StringWriter errors = new StringWriter();
	
	public String takeScreenShotOnFailure(ITestResult testResult, WebDriver driver, Method method, String sauceLabsJobIdLink) throws IOException {			

		TestRailUtilities tr = new TestRailUtilities();
		String caseId = tr.getCaseId(method);					
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String error = "";
		try {
			error = getStackTrace(testResult, method);
			logger.error(BaseTest.getMethodName() + " - " + error + "\n");				
		} catch (Exception e) {
			logger.error("EXCEPTION: getStackTrace(testResult, method);");
			e.printStackTrace(new PrintWriter(errors));				
			logger.error(errors);
		}
		if (!BaseTest.sauceLabs) {
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
		if (BaseTest.updTestRail) {
			try {
				String testId = tr.uploadResults(method, testResult, error, sauceLabsJobIdLink);				
				tr.getTestResults(testId);				
				tr.updateCase(method, "3", testResult, sauceLabsJobIdLink);
			} catch (Exception e) {
				logger.error("EXCEPTION: tr.uploadResults(projectId, suiteId, method, testResult, error, sauceLabsJobId);");
				e.printStackTrace(new PrintWriter(errors));				
				logger.error(errors);
			}
		}
		return error;
	}
}

