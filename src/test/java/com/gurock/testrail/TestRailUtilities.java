package com.gurock.testrail;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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
		APIClient client = new APIClient("https://mercatus.testrail.net/");
		client.setUser("development@gomercatus.com");
		client.setPassword("r8Po*^OfUr%rK^BWi");
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
			return suiteName + "Tests";
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

	public String getRunId(String runId, String projectId) {
		if (runId != null && runId != "") {
			return runId;
		} else {
			APIClient client = getClient();
			runId = null;
			try {
				JSONArray resultArray = (JSONArray) client.sendGet("get_runs/" + projectId);
				JSONObject jsonTestItem = (JSONObject) resultArray.get(0);
				return jsonTestItem.get("id").toString();
			} catch (Exception e) {}
			return runId;	
		}
	}

	public String uploadResults(Method method, ITestResult result, String comment, String cloudTestLink) {			

		String caseId = getCaseId(method);				
		String statusId = null;
		cloudTestLink = cloudTestLink != null ? cloudTestLink : "";
		Map<String, String> data = new HashMap<String, String>();
		StringBuffer cmnt = new StringBuffer(comment); 
		
		if (result.getStatus() == ITestResult.SUCCESS) {
			statusId = "1";
		} else if (result.getStatus() == ITestResult.FAILURE) {
			statusId = "5";	
		} else {
			logger.info("uploadResults failed. stopping the tests.");
			System.exit(0);
		}
		
		// Add parameters to the hash map
		data.put("status_id", statusId);
		
		GetTestCases gt = new GetTestCases();		
		StringBuffer steps = new StringBuffer(gt.getAutomatedTestCaseSteps(method.getName()));
		String dupCaseResults = null;
				
		if (steps.indexOf("getCaseResults") != -1) {
			
			String dupMethod = "";
			String dupCaseId = "";
			String[] s1 = steps.toString().split("\n");
			
			for (int i = 0; i < s1.length - 1; i++) {
				if (!s1[i].contains("src") && !s1[i].contains("@Test") && !s1[i].contains("void") && !s1[i].contains("//") && !s1[i].contains("new ") && s1[i].contains("_")) {
					String[] s2 = s1[i].split(" ");
					dupMethod = s2[1].replace("();", "").trim();
					String[] s3 = dupMethod.split("_");
					dupCaseId = s3[0];
					if (dupCaseId.contains(".")) {
						String[] s4 = dupCaseId.split("\\.");
						dupCaseId = s4[1];
					}
					break;
				}
			}
			
			dupCaseResults = this.getCaseResults(BaseTest.projectId, BaseTest.suiteId, dupCaseId);
			
			if (dupCaseResults.equals("Untested")) {
				steps.append(gt.getAutomatedTestCaseSteps(dupMethod));
			} else {
				steps.append("\n" + dupCaseResults); 
				cmnt.append(steps.toString()); 
			}
		} 			
			
		if (dupCaseResults == null || dupCaseResults.contains("Untested")) {
			String results = TestConfiguration.getTestRailConfig().getString("url") + "cases/results/" + BaseTest.getTestCaseId().replace("c", "");
				
			steps.append("\nREPO: " + BaseTest.repo + "\n\nSCREENCAST: " + cloudTestLink);
			cmnt.append("\n\n" + steps + "\n\nHISTORY: " + results);
			
			if (!cloudTestLink.contains(BaseTest.getBuildUrl())) {
				cmnt.append("\n\nBUILD: " + BaseTest.getBuildUrl());
			}
		}
		
		comment = cmnt.toString();

		if (comment.contains("JIRA bug")) {
			
			String[] s = comment.split(" : ");
			String bug = s[0].replaceAll("JIRA bug - ", "");			
			if (!bug.matches("null")) {
				data.put("defects", bug);
			}
			comment = comment.replaceAll("JIRA bug - " + bug + " : ", "ERROR: ");
		}
		
		data.put("comment", comment);
		
		try {
			String uri = "add_result_for_case/" + BaseTest.runId + "/" + caseId;
			JSONObject response = (JSONObject) client.sendPost(uri, data);

			if (response == null) {
				// Write the test results into file
				writeResultsToFile(caseId, statusId);
			}
			
			return response.get("test_id").toString();
		} catch (APIException e) {
			logger.error(caseId + " - " + e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(caseId + " - " + e.getMessage());
		} catch (IOException e) {
			logger.error(caseId + " - " + e.getMessage());
		}
		return null;
	}

	public ArrayList<TestcaseModel> getTestCases(String projectId, String suiteId, String[] tests) {
				
		ArrayList<TestcaseModel> list = new ArrayList<TestcaseModel>();
		try {
			for (String t : tests) {
				JSONObject jsonTestItem = (JSONObject) client.sendGet("get_case/" + t);						
				TestcaseModel testcaseModel = new TestcaseModel();
				testcaseModel.setDescription(jsonTestItem.get("title").toString());						
				testcaseModel.setTestID(jsonTestItem.get("id").toString());				
				testcaseModel.setType(this.getType(jsonTestItem.get("type_id").toString()));				
				list.add(testcaseModel);
				logger.info("getting test case id: " + jsonTestItem.get("id").toString());
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

		Map<String, String> data = new HashMap<String, String>();

		// Add parameters to the hash map
		data.put("comment", comment);
		try {
			String uri = "add_result_for_case/" + BaseTest.runId + "/" + caseId;
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
		if (!tag.contains("jenkins")) {
			tag = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			runName = "automation - ui - " + env + getSuiteName(true, BaseTest.suiteId) + " - " + BaseTest.os + " - " + browser + " - " + tag;			 
		} else {
			ExecuteShellCommand es = new ExecuteShellCommand();
			String revision = es.executeCommand("git rev-parse refs/remotes/origin/master^{commit} # timeout=10").substring(0, 7);
			runName = tag + " - commit:" + revision;
		}
		
		addRunUsingJmeter(runName.toLowerCase(), GetTestCases.getAutomatedTests());
		
		try {
			JSONArray resultArray = (JSONArray) client.sendGet("get_runs/"
					+ BaseTest.projectId);
			
			JSONObject jsonTestItem = (JSONObject) resultArray.get(0);												
			String runId = jsonTestItem.get("id").toString();
			String name = jsonTestItem.get("name").toString();
			data.add(runId);
			data.add(name);
			return data;
			
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
		return null;
	}
	
	public void updateCase(Method method, String typeId, ITestResult result, String cloudTestLink) {

		String caseId = getCaseId(method);
		Map<String, String> data = new HashMap<String, String>();

		// Add parameters to the hash map
		data.put("custom_auto_status", typeId);
		try {
			String uri = "update_case/" + caseId;
			client.sendPost(uri, data);
		} catch (APIException e) {
			logger.error(caseId + " - " + e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(caseId + " - " + e.getMessage());
		} catch (IOException e) {
			logger.error(caseId + " - " + e.getMessage());
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
			try {
				JSONArray resultArray = (JSONArray) getClient().sendGet("get_results_for_case/" + BaseTest.runId + "/" + caseId.replace("c", ""));
				if (!resultArray.isEmpty()) {
					//logger.info(resultArray);	
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
				
				String details = " - caseId : " + caseId + " | testId : " + testId + "\n\n" + comments;
				
				if (statusId.equals("1")) {			
					return "Passed" + details;					
				} else {
					return "Failed" + details;
				}
			} catch (APIException e) {
				logger.error(caseId + " - " + e.getMessage());
			} catch (MalformedURLException e) {
				logger.error(caseId + " - " + e.getMessage());
			} catch (IOException e) {
				logger.error(caseId + " - " + e.getMessage());
			}
			return "Untested";
		}
	}
	
	public String getTestId(String projectId, String suiteId, String caseId) {
		
		if (!BaseTest.updTestRail) {
			return "Untested";
		} else {						
			try {
				JSONArray resultArray = (JSONArray) getClient().sendGet("get_results_for_case/" + BaseTest.runId + "/" + caseId);
				if (!resultArray.isEmpty()) {
					//logger.info(resultArray);	
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
				logger.error(caseId + " - " + e.getMessage());
			} catch (MalformedURLException e) {
				logger.error(caseId + " - " + e.getMessage());
			} catch (IOException e) {
				logger.error(caseId + " - " + e.getMessage());
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
		
		String[] cmd4 = new String[] {"bash", "shell scripts/disable_jmeter_tests.sh", "*.jmx", "-xe"};
		es.executeArrayCommand(cmd4);
		
		String[] cmd5 = new String[] {"bash", "shell scripts/disable_jmeter_tests.sh", "addTestRailRun.jmx", "-x"};
		es.executeArrayCommand(cmd5);
		
		String jenkinsHome = System.getProperty("JENKINS_HOME");
		jenkinsHome = jenkinsHome != null ? jenkinsHome : System.getProperty("user.home") + "/.jenkins";
		
		String[] cmd6 = new String[] {jenkinsHome + "/tools/hudson.tasks.Maven_MavenInstallation/Maven/bin/mvn", "jmeter:jmeter"};
		es.executeArrayCommand(cmd6);
				
		es.executeCommand("cp " + jmx.replace("addTestRailRun", "addTestRailRunCopy") + " " + jmx);
		
		String[] cmd7 = new String[] {"bash", "shell scripts/disable_jmeter_tests.sh", "*.jmx", "-xe"};
		es.executeArrayCommand(cmd7);
	}
	
	public String getSections(String caseId) {
		try {
			JSONObject jsonTestItem = (JSONObject) getClient().sendGet("get_case/" + caseId);
			String sectionId = jsonTestItem.get("section_id").toString();
			StringBuffer sections = new StringBuffer("");
			JSONObject jsonTestItem2;
						
			do {
				jsonTestItem2 = (JSONObject) getClient().sendGet("get_section/" + sectionId);
				sections.append("" + jsonTestItem2.get("name").toString().replace(" ", "_") + "\", \"");
				try {
					sectionId = jsonTestItem2.get("parent_id").toString();
				} catch (Exception e) {
					sectionId = null;
				}
			} while (sectionId != null);
			
			return sections.substring(0, sections.length() - 2);
			
		} catch (IOException | APIException e) {}
		return null;
	}
	
	public String getSection(String caseId) {
		try {
			JSONObject jsonTestItem = (JSONObject) getClient().sendGet("get_case/" + caseId);
			String sectionId = jsonTestItem.get("section_id").toString();
			
			JSONObject jsonTestItem2 = (JSONObject) getClient().sendGet("get_section/" + sectionId);
			return jsonTestItem2.get("name").toString();
		} catch (IOException | APIException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void getTestResults(String testId) throws IOException {
		SeleniumUtils su = new SeleniumUtils();
		WebDriver dvr = Driver.getDriver(); 
		dvr.get(TestConfiguration.getTestRailConfig().getString("url") + "auth/login/");
		su.fillTxt(By.id("name"), TestConfiguration.getTestRailConfig().getString("username"));
		su.fillTxt(By.id("password"), TestConfiguration.getTestRailConfig().getString("password"));
		su.clickElement(By.cssSelector("button[type='submit']"));
		dvr.get(TestConfiguration.getTestRailConfig().getString("url") + "tests/view/" + testId);			
		su.sendkeys(By.tagName("body"), Keys.PAGE_DOWN);
	}
	
	public void logPerfResults(String comment, String perfMethodCaseId) {			
				
		Map<String, String> data = new HashMap<String, String>();
		data.put("comment", comment);
		try {
			String uri = "add_result_for_case/" + BaseTest.runId + "/" + perfMethodCaseId;
			client.sendPost(uri, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
