package com.automation.testng;

import java.util.Map;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestResult;

public class MyResultListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFinish(ISuite suite) {
		Map<String, ISuiteResult> resultMap = suite.getResults();
		for (Map.Entry<String, ISuiteResult> ent : resultMap.entrySet()) {
			ISuiteResult res = ent.getValue();
			IResultMap failedTestMap = res.getTestContext().getFailedTests();
			IResultMap passTestMap = res.getTestContext().getPassedTests();
			for (ITestResult testResult : failedTestMap.getAllResults()) {
				failedTestMap.removeResult(testResult);
				passTestMap.addResult(testResult, testResult.getMethod());
			}
		}
	}
}