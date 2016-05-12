package com.automation.exceptionHandling;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.ITestResult;

import com.automation.tests.BaseTest;
import com.automation.utils.Log4J;

public class CustomException extends Log4J{
	
	String containsTxt = "automation"; 
	
	public void throwException(String msg) {
		try {
			throw new Exception(msg);
		} catch (Exception e) {
			StackTraceElement[] st = e.getStackTrace();
			for (int i = 0; i < st.length; i++) {
				String stackTrace = st[i].toString();
				if (stackTrace.contains(containsTxt)) {					
					logger.error(BaseTest.getMethodName() + " - " + msg + "\n" + stackTrace + "\n");
				}
			}
		}
		// need to assert to fail the method after throwing the exception
		Assert.assertTrue(false);
	}
	
	public String getStackTrace(ITestResult testResult, Method method) {
		String stackTrace = null;
		int stackTraceLength = testResult.getThrowable().getStackTrace().length;
		for (int i = 0; i < stackTraceLength; i++) {
			stackTrace = testResult.getThrowable().getStackTrace()[i].toString();
			if (stackTrace.contains(containsTxt) && !stackTrace.contains("SeleniumUtils") && !stackTrace.contains("CustomException")) {
				
				String methodName = method.getName();
				String[] s = methodName.split("_");
				methodName = s[0];
				stackTrace = stackTrace.replace(method.getName(), methodName); 
				break;
			}
		}
		String[] s = testResult.getThrowable().toString().split("\n");
		stackTrace = s[0].toString() + "." + stackTrace;
		return stackTrace;
	}
	
	public String getStackTrace(Exception ex) {
		String stackTrace = null;
		for (int i = 0; i < ex.getStackTrace().length; i++) {
			stackTrace = ex.getStackTrace()[i].toString();
			if (stackTrace.contains(containsTxt) && !stackTrace.contains("SeleniumUtils") && !stackTrace.contains("CustomException")) {				
				return stackTrace; 
			}
		}
		return stackTrace;
	}
}
