package com.automation.tests

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.automation.config.TestConfiguration;
import com.automation.data.DatabaseUtilities;
import com.automation.selenium.Driver;
import com.automation.selenium.SauceLabs;
import com.automation.selenium.SeleniumUtils;
import com.automation.tests.BaseTest;
import com.automation.utils.DateValidator;
import com.gurock.testrail.TestRailUtilities;

public class MasterTests extends BaseTest {
	
	String perfMethodCaseId = "";
	StringBuffer perfTestsResponseTimes = new StringBuffer();
	boolean isPerfTest = false;
	
	@BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method){
        Test testClass = method.getAnnotation(Test.class);

        for (int i = 0; i < testClass.groups().length; i++) {
            if (testClass.groups()[i].contains("Performance")) {
            	isPerfTest = true;
            	perfMethodCaseId = BaseTest.getTestCaseId().replace("c", "");
            	break;
            }
        }
    }
	
	public void logPerfTestResponseTimesAndScreencasts() {
		if (isPerfTest && cloudTest && updTestRail) {
			String runId = getRunId(BaseTest.runId, BaseTest.projectId, BaseTest.suiteId);
			Map<String, String> data = new HashMap<String, String>();		
			SauceLabs sl = new SauceLabs();
			List<String> screencasts = sl.getScreencasts(perfMethodCaseId, 10);
			StringBuilder sb = new StringBuilder();
			int i = 1;
			for (String sc : screencasts) {
				sb.append(i	+ " - " + sc + "\n");
				i++;
			}
			data.put("comment", "SCREENCASTS:\n\n" + sb.toString());
			try {
				String uri = "add_result_for_case/" + runId + "/" + perfMethodCaseId;
				TestRailUtilities tr = new TestRailUtilities();
				tr.getClient().sendPost(uri, data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.logPerfResults(perfTestsResponseTimes.toString(), perfMethodCaseId);				
		}	
	}
	
	@AfterTest(alwaysRun = true)
	public void afterTest() {
		logPerfTestResponseTimesAndScreencasts();
	}
}
