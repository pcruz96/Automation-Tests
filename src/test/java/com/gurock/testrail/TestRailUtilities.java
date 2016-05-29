package com.gurock.testrail;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

import com.automation.config.TestConfiguration;
import com.automation.selenium.Driver;
import com.automation.selenium.SeleniumUtils;
import com.automation.tests.BaseTest;
import com.automation.utils.CamelCaseString;
import com.automation.utils.ExecuteShellCommand;
import com.automation.utils.Log4J;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

public class TestRailUtilities extends Log4J {
	
	APIClient client;
	
	public TestRailUtilities() {
		client = getClient();
	}
	
    public String getBuildTag() {
        String buildTag;
        if (System.getenv("BUILD_TAG") == null) {
            buildTag = "";
        } else {
            buildTag = "-" + System.getenv("BUILD_TAG");
        }
        return buildTag;
    }

	public APIClient getClient() {
		APIClient client = new APIClient("https://autotests.testrail.net/");
		client.setUser("pcruz96@yahoo.com");
		client.setPassword("XykKZUA115OZkNdd8eD6");
		return client;
	}
	
	public String getProjectName(String projectId) {
		String projectName = null;
		try {
			JSONObject jsonTestItem = (JSONObject) client.sendGet("get_project/" + projectId);				
			projectName = jsonTestItem.get("name").toString().toLowerCase();
			return projectName;
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return projectName;
	}

	public String getSuiteName(boolean addRun, String suiteId) {		
		String suiteName = null;
		try {
			JSONObject jsonTestItem = (JSONObject) client.sendGet("get_suite/" + suiteId);								
			suiteName = jsonTestItem.get("name").toString();
			if (addRun) {
				suiteName = jsonTestItem.get("name").toString();	
			} else {
				suiteName = CamelCaseString.toCamelCase(suiteName);
			}						
			return suiteName;
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return suiteName;
	}

	public String getCaseId(Method method) {
		String testCase = method.getName();
		String[] tc = testCase.split("_");
		String caseId = tc[0].toString().replace("c", "");
		return caseId;
	}

	public String getRunId(String runId, String projectId, String suiteId) {
		if (runId != null && runId != "") {
			return runId;
		} else {
			APIClient client = getClient();
			runId = null;
			try {
				JSONArray resultArray = (JSONArray) client.sendGet("get_runs/" + projectId);
				JSONObject jsonTestItem = (JSONObject) resultArray.get(0);
				return jsonTestItem.get("id").toString();
			} catch (APIException e) {
				logger.error(e.getMessage());
			} catch (MalformedURLException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			return runId;	
		}
	}

	public String uploadResults(Method method, ITestResult result, String comment, String sauceLabUrl) {			

		String caseId = getCaseId(method);
		String runId = getRunId(BaseTest.runId, BaseTest.projectId, BaseTest.suiteId);		
		String statusId = null;
		Map<String, String> data = new HashMap<String, String>();
		
		if (result.getStatus() == ITestResult.SUCCESS) {
			statusId = "1";
		} else if (result.getStatus() == ITestResult.FAILURE) {
			statusId = "5";	
			/*
			try {
				CustomException ce = new CustomException();
				String error = ce.getStackTrace(result, method);
				if (error.length() > 250) {
					error = "..." + error.substring(error.indexOf("automation"), error.length());
				}
				data.put("defects", error);
			} catch (Exception e) {
				data.put("defects", "");
			}
			*/
		}
		
		// Add parameters to the hash map
		data.put("status_id", statusId);
		
		String steps = GetTestCases.getAutomatedTestCaseSteps(getSuiteName(false, BaseTest.suiteId), method.getName());
		steps += "\n" + BaseTest.repo + "\n\n" + sauceLabUrl;
		comment += "\n\n" + steps;

		if (!sauceLabUrl.contains(BaseTest.getBuildUrl())) {
			comment += "\n\n" + BaseTest.getBuildUrl();
		}
		
		if (!BaseTest.sauceLabs) {
			comment += "\n\n ran locally";
		}
		
		data.put("comment", comment);
		try {
			String uri = "add_result_for_case/" + runId + "/" + caseId;
			JSONObject response = (JSONObject) client.sendPost(uri, data);

			if (response == null) {
				// Write the test results into file
				writeResultsToFile(caseId, statusId);
			}
			
			return response.get("test_id").toString();
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public ArrayList<TestcaseModel> getTestCases(String projectId, String suiteId) {
				
		ArrayList<TestcaseModel> list = new ArrayList<TestcaseModel>();
		try {
			JSONArray resultArray = (JSONArray) client.sendGet("get_cases/"
					+ projectId + "&suite_id=" + suiteId);
			
			for (int i = 0; i < resultArray.size(); i++) {
				JSONObject jsonTestItem = (JSONObject) resultArray.get(i);
				TestcaseModel testcaseModel = new TestcaseModel();
				testcaseModel.setDescription(jsonTestItem.get("title")
						.toString());
				testcaseModel.setTestID(jsonTestItem.get("id").toString());				
				testcaseModel.setType(this.getType(jsonTestItem.get("type_id").toString()));				
				list.add(testcaseModel);
			}

		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return list;
	}

	private void writeResultsToFile(String caseId, String results) {
		try {
			File file = new File("test-output/results.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(caseId + "   " + results + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addComment(String projectId, String suiteId, Method method,
			String comment) {

		String caseId = getCaseId(method);
		String runId = getRunId(BaseTest.runId, BaseTest.projectId, BaseTest.suiteId);

		Map<String, String> data = new HashMap<String, String>();

		// Add parameters to the hash map
		data.put("comment", comment);
		try {
			String uri = "add_result_for_case/" + runId + "/" + caseId;
			JSONObject response = (JSONObject) client.sendPost(uri, data);
			if (response == null) {
				// Write the test results into file
				writeResultsToFile(caseId, comment);
			}
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public List<String> addRun(String env, String browser) {

		List<String> data = new ArrayList<String>();
		String tag = this.getBuildTag().replace("-", " ");
		String runName = "";
		if (tag == "") {
			tag = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			runName = "automation - ui - " + env + getSuiteName(true, BaseTest.suiteId) + " - " + tag;
		} else {
			runName = "automation - ui - " + env + tag;
		}
		
		addRunUsingJmeter(runName.toLowerCase(), GetTestCases.getAutomatedTests(BaseTest.suiteName));
		
		try {
			JSONArray resultArray = (JSONArray) client.sendGet("get_runs/"
					+ BaseTest.projectId);
			
			for (int i = 0; i < resultArray.size(); i++) {
				JSONObject jsonTestItem = (JSONObject) resultArray.get(i);												
				String runId = jsonTestItem.get("id").toString();
				String name = jsonTestItem.get("name").toString();
				data.add(runId);
				data.add(name);
				return data;
			}
			
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (APIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		// Add parameters to the hash map
		data.put("suite_id", BaseTest.suiteId);		
		data.put("name", runName.toLowerCase());		
		//List<Integer> caseIds = GetTestCases.getAutomatedTests(BaseTest.suiteName);		
		//data.put("case_ids", caseIds);
				
		try {
			String uri = "add_run/" + BaseTest.projectId;
			JSONObject response = (JSONObject) client.sendPost(uri, data);

			if (response == null) {
				// Write the test results into file
				logger.error("error creating new run for projectId/suiteId, " + BaseTest.projectId + "/" + BaseTest.suiteId);
			} else {
				return response.get("id").toString();
			}					
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		*/
		return null;
	}
	
	public void updateCase(Method method, String typeId, ITestResult result, String sauceLabUrl) {

		String caseId = getCaseId(method);
		Map<String, String> data = new HashMap<String, String>();

		// Add parameters to the hash map
		data.put("type_id", typeId);
		/*
		if (result.getStatus() == ITestResult.FAILURE) {
			try {
				CustomException ce = new CustomException();
				String error = ce.getStackTrace(result, method);
				if (error.length() > 250) {
					error = error.substring(0,250);
				}
				data.put("refs", error);
			} catch (Exception e) {
				data.put("refs", "");
			}
		} else {
			data.put("refs", "");
		}
		*/
		try {
			String uri = "update_case/" + caseId;
			client.sendPost(uri, data);
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void closeRun(String runId) {
		try {
			String uri = "close_run/" + runId;
			JSONObject response = (JSONObject) client.sendPost(uri, runId);

			if (response == null) {
				logger.error("error closing run for run id, " + runId);
			} else {
				logger.info("closed run id, " + runId);
			}
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}	
	}
	
	public void closeRunsContainingTxt(String projectId, String txt) {

		String runId = null;
		String isCompleted = null;
		try {
			JSONArray resultArray = (JSONArray) client.sendGet("get_runs/"
					+ projectId);

			for (int i = (resultArray.size()); i < resultArray.size(); i--) {
				JSONObject jsonTestItem = (JSONObject) resultArray.get(i);
				String getRunName = jsonTestItem.get("name").toString();				
				isCompleted = jsonTestItem.get("is_completed").toString();
				if (getRunName.contains(txt) && isCompleted == "false") { 
					runId = jsonTestItem.get("id").toString();
					this.closeRun(runId);
				}
			}
			logger.info("completed closing runs containing the text, " + txt);
		} catch (APIException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public String getCaseResults(String projectId, String suiteId, String caseId) {
		
		if (!BaseTest.updTestRail) {
			return "Untested";
		} else {			
			String runId = getRunId(null, projectId, suiteId);
			try {
				JSONArray resultArray = (JSONArray) getClient().sendGet("get_results_for_case/" + runId + "/" + caseId.replace("c", ""));
				if (!resultArray.isEmpty()) {
					logger.info(resultArray);	
				}			
				String testId = null;
				String statusId = null;
				String comments = null;
				try {
					JSONObject jsonTestItem = (JSONObject) resultArray.get(0);
					testId = jsonTestItem.get("test_id").toString();								
					statusId = jsonTestItem.get("status_id").toString();
					comments = jsonTestItem.get("comment").toString();				
					logger.info("\n\nTEST ID = " + testId + " | CASE ID = " + caseId + "\n");
									
				} catch (Exception e) {
					return "Untested";
				}
				if (statusId.equals("1")) {			
					return "Passed - projectId : " + projectId + " = " + this.getProjectName(projectId) + " | suiteId : " 
							+ suiteId + " = " + this.getSuiteName(false, suiteId) + " | caseId : " + caseId + " | testId : " 
							+ testId + "\n\n" + comments;
				} else {
					return "Failed";
				}
			} catch (APIException e) {
				logger.error(e.getMessage());
			} catch (MalformedURLException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			return "Untested";
		}
	}
	
	public String getTestId(String projectId, String suiteId, String caseId) {
		
		if (!BaseTest.updTestRail) {
			return "Untested";
		} else {			
			String runId = getRunId(null, projectId, suiteId);
			try {
				JSONArray resultArray = (JSONArray) getClient().sendGet("get_results_for_case/" + runId + "/" + caseId);
				if (!resultArray.isEmpty()) {
					logger.info(resultArray);	
				}			
				String testId = null;
				try {
					JSONObject jsonTestItem = (JSONObject) resultArray.get(0);
					testId = jsonTestItem.get("test_id").toString();
					return testId;
									
				} catch (Exception e) {
					return null;
				}
			} catch (APIException e) {
				logger.error(e.getMessage());
			} catch (MalformedURLException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			return null;
		}
	}
	
	public String getType(String typeId) {
		try {
			JSONArray resultArray = (JSONArray) getClient().sendGet("get_case_types");
			
			for (int i = 0; i < resultArray.size(); i++) {
				JSONObject jsonTestItem = (JSONObject) resultArray.get(i);				
				if (jsonTestItem.get("id").toString().equals(typeId)) {
					return jsonTestItem.get("name").toString();					
				}
			}
		} catch (IOException | APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void addRunUsingJmeter(String runName, Object caseIds) {
		String jmx = "src/test/jmeter/addTestRailRun.jmx";
		ExecuteShellCommand es = new ExecuteShellCommand();
		es.executeCommand("cp " + jmx + " " + jmx.replace("addTestRailRun", "addTestRailRunCopy"));
				
		String[] cmd = new String[] {"sed", "-i.tmp", "s/PROJECTID/"+BaseTest.projectId+"/g", jmx};
		es.executeArrayCommand(cmd);
				
		String[] cmd1 = new String[] {"sed", "-i.tmp", "s/SUITEID/"+BaseTest.suiteId+"/g", jmx};
		es.executeArrayCommand(cmd1);
		
		String[] cmd2 = new String[] {"sed", "-i.tmp", "s/RUNNAME/"+runName+"/g", jmx};
		es.executeArrayCommand(cmd2);
		
		String[] cmd3 = new String[] {"sed", "-i.tmp", "s/CASEIDS/"+caseIds+"/g", jmx};
		es.executeArrayCommand(cmd3);
		
		String[] cmd4 = new String[] {"bash", "disable_jmeter_tests.sh", "*.jmx", "-xe"};
		es.executeArrayCommand(cmd4);
		
		String[] cmd5 = new String[] {"bash", "disable_jmeter_tests.sh", "addTestRailRun.jmx", "-x"};
		es.executeArrayCommand(cmd5);
		
		String[] cmd6 = new String[] {System.getProperty("user.home") + "/.jenkins/tools/hudson.tasks.Maven_MavenInstallation/Maven/bin/mvn", "jmeter:jmeter"};
		es.executeArrayCommand(cmd6);
				
		es.executeCommand("cp " + jmx.replace("addTestRailRun", "addTestRailRunCopy") + " " + jmx);
		
		String[] cmd7 = new String[] {"bash", "disable_jmeter_tests.sh", "*.jmx", "-xe"};
		es.executeArrayCommand(cmd7);
	}
	
	public String getSection(String caseId) {
		try {
			JSONObject jsonTestItem = (JSONObject) getClient().sendGet("get_case/" + caseId);
			String sectionId = jsonTestItem.get("section_id").toString();
			
			JSONObject jsonTestItem2 = (JSONObject) getClient().sendGet("get_section/" + sectionId);
			return jsonTestItem2.get("name").toString();
		} catch (IOException | APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void getTestResultScreenshot(String testId) throws IOException {
		SeleniumUtils su = new SeleniumUtils();
		WebDriver dvr = Driver.getDriver(); 
		dvr.get(TestConfiguration.getTestRailConfig().getString("url") + "auth/login/");
		su.fillTxt(By.id("name"), TestConfiguration.getTestRailConfig().getString("username"));
		su.fillTxt(By.id("password"), TestConfiguration.getTestRailConfig().getString("password"));
		su.clickElement(By.cssSelector("button[type='submit']"));
		dvr.get(TestConfiguration.getTestRailConfig().getString("url") + "tests/view/" + testId);
		File scrFile = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File("test-output/screenshots/"+BaseTest.getTestCaseId()+"-1.png"));					
			su.sendkeys(By.tagName("body"), Keys.PAGE_DOWN);
			su.threadSleep(3000);
			File scrFile2 = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile2, new File("test-output/screenshots/"+BaseTest.getTestCaseId()+"-2.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
