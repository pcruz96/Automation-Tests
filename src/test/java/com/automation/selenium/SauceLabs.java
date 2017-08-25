package com.automation.selenium;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.openqa.selenium.remote.SessionId;
import org.testng.ITestResult;

import com.automation.config.TestConfiguration;
import com.automation.utils.HMac;
import com.automation.utils.Log4J;
import com.saucelabs.saucerest.SauceREST;

public class SauceLabs extends Log4J {
	
	private static final String USER = TestConfiguration.getSauceLabsConfig().getString("USER");
	private static final String KEY = TestConfiguration.getSauceLabsConfig().getString("KEY");
	StringWriter errors = new StringWriter();	

	public String getLinkToSauceLabJobId(String jobId)
			throws UnsupportedEncodingException {

		String jobIdLink = null;
		HMac hm = new HMac();
		String authToken = null;
		try {
			authToken = hm.getAuthToken(jobId);
			jobIdLink = "https://saucelabs.com/tests/" + jobId + "?auth=" + authToken;							
			return jobIdLink;							
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException
				| IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}
		return jobIdLink;
	}
	
	public void updateSauceLabTestResult(SessionId jobId, ITestResult testResult) {
		SauceREST saucerest = new SauceREST(USER, KEY);
    	
		if (testResult.getStatus() == ITestResult.SUCCESS) {				

			saucerest.jobPassed(jobId.toString());
	        	        	
        } else if (testResult.getStatus() == ITestResult.FAILURE) {
        	
        	saucerest.jobFailed(jobId.toString());	 
		}	
	}
}
