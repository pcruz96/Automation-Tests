package com.automation.selenium;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.remote.SessionId;
import org.testng.ITestResult;

import com.automation.config.TestConfiguration;
import com.automation.utils.ExecuteShellCommand;
import com.automation.utils.HMac;
import com.automation.utils.Log4J;
import com.saucelabs.saucerest.SauceREST;

public class SauceLabs extends Log4J {
	
	String user = TestConfiguration.getSauceLabsConfig().getString("USER");
	String apiKey = TestConfiguration.getSauceLabsConfig().getString("APIKEY");
	String key = TestConfiguration.getSauceLabsConfig().getString("KEY");
	StringWriter errors = new StringWriter();
	
	public String getJobId(Method method) {
		ExecuteShellCommand es = new ExecuteShellCommand();
		String jobs = es.executeCommand("curl -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs?full=true&limit=50");
		JSONArray array;
		try {
			array = (JSONArray) new JSONParser().parse(jobs);
			for (int i = 0; i < array.size(); i++) {
				JSONObject json = (JSONObject) array.get(i);
				if (json.get("name").equals(method.getName())) {
					return json.get("id").toString();
				}
			}			
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public String getJobId(String caseId) {
		ExecuteShellCommand es = new ExecuteShellCommand();
		String jobs = es.executeCommand("curl -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs?full=true&limit=50");
		JSONArray array;
		try {
			array = (JSONArray) new JSONParser().parse(jobs);			
			for (int i = 0; i < array.size(); i++) {
				JSONObject json = (JSONObject) array.get(i);
				if (((String) json.get("name")).contains(caseId)) {
					return json.get("id").toString();		
				}
			}			
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		return null;
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
	
	public void updateSauceLabTestResult(SessionId jobId, ITestResult testResult) {
		try {
        	SauceREST saucerest = new SauceREST(user, key);
        	//logger.info(jobId);
        		        	
			if (testResult.getStatus() == ITestResult.SUCCESS) {				

				saucerest.jobPassed(jobId.toString());
		        	        	
	        } else if (testResult.getStatus() == ITestResult.FAILURE) {
	        	
	        	saucerest.jobFailed(jobId.toString());	 
			}							
			
		} catch (Exception e) {
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}
	}
	
	public String getScreencast(String method) {
		ExecuteShellCommand es = new ExecuteShellCommand();
		String jobs = es.executeCommand("curl -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs?full=true&limit=50");
		JSONArray array;
		try {
			array = (JSONArray) new JSONParser().parse(jobs);

			for (int i = 0; i < array.size(); i++) {
				
				JSONObject json = (JSONObject) array.get(i);
				
				if (((String) json.get("name")).equals(method)) {

					String jobId = json.get("id").toString();
					String cloudTestJobIdLink = null;
					try {
						cloudTestJobIdLink = this.getLinkToSauceLabJobId(jobId);
						return cloudTestJobIdLink;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}			
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return null;
	}
}
