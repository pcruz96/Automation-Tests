package com.automation.selenium;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.testng.ITestResult;

import com.automation.config.TestConfiguration;
import com.automation.utils.ExecuteShellCommand;
import com.automation.utils.HMac;
import com.automation.utils.Log4J;

public class SauceLabs extends Log4J {
	
	String user = TestConfiguration.getSauceLabsConfig().getString("USER");
	String apiKey = TestConfiguration.getSauceLabsConfig().getString("APIKEY");
	StringWriter errors = new StringWriter();
	
	public String getJobId(Method method) {
		String jobId = null;
		ExecuteShellCommand es = new ExecuteShellCommand();
		String jobsFullInfo = es.executeCommand("curl -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs?full=true&limit=50");
		String[] array = null;
		if (jobsFullInfo.contains("browser_short_version")) {
			array = jobsFullInfo.split("browser_short_version");
		} else if (jobsFullInfo.contains("browser_version")) {
			array = jobsFullInfo.split("browser_version");
		}
		for (String s : array) {
			if (s.contains(method.getName())) {
				String[] array2 = s.split(",");
				for (String s2 : array2) {
					if (s2.contains("\"id\"")) {
						String[] array3 = s2.toString().split(":");
						jobId = array3[1].toString().replace("\"", "").trim();
						return jobId;
					}
				}
			}
		}
		return jobId;
	}
	
	public String getJobId(String caseId) {
		String jobId = null;
		ExecuteShellCommand es = new ExecuteShellCommand();
		String jobsFullInfo = es.executeCommand("curl -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs?full=true&limit=50");
		String[] array = null;
		if (jobsFullInfo.contains("browser_short_version")) {
			array = jobsFullInfo.split("browser_short_version");
		} else if (jobsFullInfo.contains("browser_version")) {
			array = jobsFullInfo.split("browser_version");
		}
		for (String s : array) {			
			if (s.contains(caseId)) {
				String[] array2 = s.split(",");
				for (String s2 : array2) {
					if (s2.contains("\"id\"")) {
						String[] array3 = s2.toString().split(":");
						jobId = array3[1].toString().replace("\"", "").trim();
						return jobId;
					}
				}
			}
		}
		return jobId;
	}

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
	
	public void createShellScriptUpdateResults(Hashtable<String, ITestResult> testResults) {
		StringBuilder sb = new StringBuilder();		
		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter("updateSauceLabsResults.sh"));
			Set<String> keys = testResults.keySet();
			boolean passed = false;
	        for(String jobId: keys){
	        	if (testResults.get(jobId).getStatus() == ITestResult.SUCCESS) {				
	        		passed = true;	        	
		        } else if (testResults.get(jobId).getStatus() == ITestResult.FAILURE) {
		        	passed = false;	 
				}
	            logger.info("job id, "+jobId+", passed is: "+passed);
	            sb.append("curl -X PUT -s -d '{\"passed\": "+passed+"}' -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs/" + jobId + "\n");
	        }			
	        output.write("#! /bin/bash\n" + sb.toString());
	        output.close();
		} catch (IOException e) {
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}
	}
}
