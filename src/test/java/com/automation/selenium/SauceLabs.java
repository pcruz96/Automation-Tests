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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
	
	public void createShellScriptUpdateResults(Hashtable<String, ITestResult> testResults) {
		StringBuilder sb = new StringBuilder();		
		BufferedWriter output;
		String file = "shell scripts/updateSauceLabsResults.sh";
		try {
			output = new BufferedWriter(new FileWriter(file));
			Set<String> keys = testResults.keySet();
			boolean passed = false;
			String jid = null;
	        for(String jobId: keys){
	        	jid = jobId;
	        	if (testResults.get(jobId).getStatus() == ITestResult.SUCCESS) {				
	        		passed = true;	        	
		        } else if (testResults.get(jobId).getStatus() == ITestResult.FAILURE) {
		        	passed = false;	 
				}
	            //logger.info("job id, "+jobId+", passed is: "+passed);
	            sb.append("curl -X PUT -s -d '{\"passed\": "+passed+"}' -u "+apiKey+" https://saucelabs.com/rest/v1/"+user+"/jobs/" + jobId + "\n");
	        }			
	        output.write("#! /bin/bash\n" + sb.toString());
	        output.close();
	        
	        String[] cmd = new String[] {"bash", file};
			ExecuteShellCommand es = new ExecuteShellCommand();
			es.executeArrayCommand(cmd);
	        
	        String[] cmd1 = new String[] {"grep", "-nr", jid, file};
			String result = es.executeArrayCommand(cmd1);
			String[] s = result.split(":");
			String[] cmd2 = new String[] {"sed", "-i.tmp", s[1] + "s/curl/#curl/g", file};
			es.executeArrayCommand(cmd2);
			
		} catch (IOException e) {
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
