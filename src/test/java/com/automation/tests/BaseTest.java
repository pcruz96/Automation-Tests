package com.automation.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.automation.config.TestConfiguration;
import com.automation.pageObjs.LoginPage;
import com.automation.selenium.BrowserStack;
import com.automation.selenium.Driver;
import com.automation.selenium.SauceLabs;
import com.automation.selenium.ScreenshotOnFailure;
import com.automation.selenium.SeleniumUtils;
import com.automation.testng.Retry;
import com.automation.utils.ExecuteShellCommand;
import com.automation.utils.FileUtilities;
import com.automation.utils.Jira;
import com.automation.utils.Log4J;
import com.automation.utils.SlackNotifications;
import com.gurock.testrail.GetTestCases;
import com.gurock.testrail.TestRailUtilities;

public class BaseTest extends TestRailUtilities {
	
	protected final static Logger logger = Logger.getLogger(Log4J.class);
	private static ThreadLocal<String> methodName = new ThreadLocal<String>();
	private static ThreadLocal<String> testCaseId = new ThreadLocal<String>();	
	private static ThreadLocal<String> maxTestDataName = new ThreadLocal<String>();
	private static ThreadLocal<String> caseResults = new ThreadLocal<String>();
	private static ThreadLocal<Boolean> mxTitles = new ThreadLocal<Boolean>();
	public static ThreadLocal<String> testDataName = new ThreadLocal<String>();
	
	boolean methodNameCorrect = false;	
	
	Hashtable<String, ITestResult> testResults = new Hashtable<String, ITestResult>();
	
	final static StringWriter errors = new StringWriter();
	final static StringBuilder sb = new StringBuilder();
	final static StringBuilder notPassedCaseIds = new StringBuilder();
	
	public static String host = null;
	public static String repo = null;
	public static String project = null;
	public static String projectId = null;
	public static String suiteId = null;
	public static String env = null;
	public static String os = null;
	public static String browser = null;
	public static String runId = null;
	public static String database = null;
	public static String databaseName = null;
	
	public static boolean updTestRail = false;	
	public static boolean addRun = false;
	public static boolean cloudTest = false;
	public static String cloudTestProvider = null;
	
	HashMap<String, String> jiraMap = new HashMap<String, String>();
	
	String runName;
	public String projectParticipant;
	public String projectParticipantFullName;
	int failCount = 0;
	
	public String getTestEnv(String testEnv, boolean tag) {
		
		String env = System.getenv("ENV");
		if (env != null) {
			testEnv = env;
		}
		if (tag) {
			return testEnv;
		} else {
			return testEnv + " - ";
		}
	}
	
	public static String getMethodName() {
		String[] s = methodName.get().split("_");
		testCaseId.set(s[0]);
        return methodName.get();
    }
	
	public static String getTestCaseId() {
		return testCaseId.get();
    }
	
	public static String getTestDataName() {
		return testDataName.get();
    }
	
	public static String getMaxTestDataName() {		
		return maxTestDataName.get();
    }
	
	public static boolean getMaxTitles() {
		try {
			return mxTitles.get();	
		} catch (Exception e) {
			return false;	
		}		
    }

	public static String getRandomUUIDString() {
		String randomUUIDString = UUID.randomUUID().toString();
		return randomUUIDString;
	}
	
	public String getTestCaseName() {		
		String testCaseName = BaseTest.getMethodName(); 		
		if (BaseTest.getMethodName().length() > 100) {
			testCaseName = testCaseName.substring(0,100);
		}		
		return testCaseName;
	}
	
	public static String getBuildUrl() {
		String buildUrl = System.getenv("BUILD_URL");
		if (buildUrl == null) {
			buildUrl = "";
		}
		return buildUrl;
	}
	
	public static String getCaseResults() {		
		String cr = caseResults.get();
		if (cr != null) {
			return cr;
		} 
		return "";
    }
		
	public void setVars() {

	}
	
	@BeforeSuite(alwaysRun = true)
	@Parameters({ "repo", "projectId", "suiteId", "env", "updateTestRail", "addRun", "runId", "cloudTest",
			"cloudTestProvider", "os", "browser", "database" })
	public void beforeSuite(String repo, String projectId, String suiteId, String env, boolean updateTestRail,
			boolean addRun, String runId, boolean cloudTest, String cloudTestProvider, String os, String browser,
			@Optional String database) {
		
		BaseTest.repo = repo;
		String[] suite = this.getClass().getName().split("\\.");		
		BaseTest.project = suite[suite.length - 2];
		BaseTest.projectId = projectId; 
		BaseTest.suiteId = suiteId;
		BaseTest.env = env.replace(".conf", "");		
		BaseTest.updTestRail = updateTestRail;	
		BaseTest.addRun = addRun;
		BaseTest.runId = runId;
		BaseTest.cloudTest = cloudTest;
		BaseTest.cloudTestProvider = cloudTestProvider;		
		BaseTest.os = os;
		BaseTest.browser = browser;
		BaseTest.database = database;
		
		FileUtilities fu = new FileUtilities();		
		File dir = new File("test-output");
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}
		new File("test-output").mkdir();
		fu.createFile("test-output/log4j-application.log");
		org.apache.log4j.PropertyConfigurator.configure("src/test/resources/log4j.properties");

		if (updTestRail && addRun) {
			
			List<String> lst = addRun(getTestEnv(env, false), browser);
			
			BaseTest.runId = lst.get(0).toString();
			runName = lst.get(1).toString();			
			logger.info("\n\nrun id | name: " + BaseTest.runId + " | " + runName + "\n");
			ExecuteShellCommand es = new ExecuteShellCommand();
			String[] cmd1 = new String[] {"sed", "-i.tmp", "s/RUNID/"+BaseTest.runId+"/g", "src/test/resources/testng/testng.xml"};			
			String[] cmd2 = new String[] {"sed", "-i.tmp", "s/BUILD_TAG/"+runName+"/g", "src/test/resources/testng/testng.xml"};			
			es.executeArrayCommand(cmd1);
			es.executeArrayCommand(cmd2);
		}			
		TestConfiguration.setConfig(env);
		BaseTest.host = TestConfiguration.getConfig().getString("login.url");
		BaseTest.databaseName = TestConfiguration.getDbConfig().getString("db." + BaseTest.env + ".name");
		createTestNGfailed(fu);
		removeTmpFiles();
	}
	
	@BeforeMethod(alwaysRun = true)
	@Parameters({ "name", "os", "os_version", "browser", "version", "deviceName", "deviceOrientation", "who" })			
	public void setup(@Optional String name, @Optional String os, @Optional String os_version,
			@Optional String browser, @Optional String version,
			@Optional String deviceName, @Optional String deviceOrientation, @Optional String who, Method method)			
			throws MalformedURLException {
		
		setVars();
		methodName.set(method.getName());
		BaseTest.getMethodName();

		if (updTestRail) {
			if (getMethodName().equals(getCaseId(method))) {
				Assert.assertEquals(getMethodName(), getMethodName() + " does not conform to the method naming convention. expected c${test rails test case id}_${desc}");
			} else {
				methodNameCorrect = true;
			}
		}
		
		testDataName.set(BaseTest.getTestCaseId() + "-" + BaseTest.getRandomUUIDString());
		testDataName.set(BaseTest.getTestDataName().substring(0, 30));		

		SeleniumUtils su = new SeleniumUtils();
		maxTestDataName.set(BaseTest.getTestDataName() + su.getRandomString(255 - BaseTest.getTestDataName().length()));

		Driver.createDriver(name, getTestEnv(env, true), os, os_version, browser, version, deviceName,
				deviceOrientation, cloudTest, method);
		
		Driver.getDriver().manage().window().maximize();		
		try {			
			LoginPage login = new LoginPage();
			who = who != null ? who : "login";
			login.login(TestConfiguration.getConfig().getString("login.username"), TestConfiguration.getConfig().getString("login.password"));
		} catch (Exception e) {
			throw new SkipException("LoginPage is not loaded");
		}
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestContext context, ITestResult result, Method method) throws IOException, ParseException {

		String cloudTestJobIdLink = "";
		String steps = null;
		
		//will display testId for faster tracking
		this.getCaseResults(projectId, suiteId, getCaseId(method));
		
		if (cloudTest) {			
			String jobId = null;
			SauceLabs sl = new SauceLabs();
			
			if (BaseTest.cloudTestProvider.equals("sauceLabs")) {				
				try {
					jobId = sl.getJobId(method);
				} catch (Exception e) {
					jobId = null;
				}
			}
			
			GetTestCases gt = new GetTestCases();			
			steps = gt.getAutomatedTestCaseSteps(method.getName());
			logger.info(steps);
			String dupResults = BaseTest.getCaseResults();
			
			steps = steps != null ? steps : "";
			dupResults = dupResults != null ? dupResults : "";			
			
			if (steps.contains("getCaseResults") && dupResults.contains("Passed")) {
				cloudTestJobIdLink = "The Sauce Labs session is of a similar test case referenced in the steps. " + dupResults;
			} else if (jobId != null) {
				testResults.put(jobId, result);
				cloudTestJobIdLink = sl.getLinkToSauceLabJobId(jobId);
				logger.info("\n" + method.getName() + " - " + cloudTestJobIdLink + "\n");
			} else {
				try {
					if (BaseTest.cloudTestProvider.equals("browserStack")) {
						BrowserStack bs = new BrowserStack();
						cloudTestJobIdLink = bs.getPublicUrl(method.getName());
						logger.info("\n" + method.getName() + " - " + cloudTestJobIdLink + "\n");
					}
				} catch (Exception e) {
					cloudTestJobIdLink = "";
				}
			}
		} 
		
		String msg = "failed - automation - ui - " + this.getTestSuite() + " - " + BaseTest.browser + " - "
				+ BaseTest.getMethodName();

		msg = msg.toLowerCase();
		
		if (updTestRail) {
			if (runId == null) {				
				runId = this.getRunId(BaseTest.runId, BaseTest.projectId);
			}					
			if (methodNameCorrect) {					
				if (result.getStatus() == ITestResult.SUCCESS) {				
					uploadResults(method, result, "", cloudTestJobIdLink);
					
					Jira j = new Jira();
					
					if (!j.isStatusClosedOrDone(msg)) {
						j.closeIssue(msg, cloudTestJobIdLink);
					}
				}
				updateCase(method, "1", result, cloudTestJobIdLink); // 1 = Automated
			}
		}
		if (Retry.retryCount == Retry.MAXRETRYCOUNT && result.getStatus() == ITestResult.FAILURE) {
			ScreenshotOnFailure ss = new ScreenshotOnFailure();
			String error = null;
			try {
				error = ss.takeScreenShotOnFailure(result, Driver.getDriver(), method, cloudTestJobIdLink);
			} catch (Exception e) {}
			
			SlackNotifications cn = new SlackNotifications();		
			
			if (updTestRail) {
				
				Jira jira = new Jira();
				String bugId = null;
				String jiraLink = "";
				
				if (jira.isNewIssueOrResolutionIsFixedOrStatusIsVerifiedOrClosed(msg)) { 
					
					String jiraDesc = null; 
					
					if (cloudTest) {
						//jiraDesc = cloudTestJobIdLink.replace("/", "\\/");
						
						String testRailUsername = TestConfiguration.getTestRailConfig().getString("username");
						String testRailPassword = TestConfiguration.getTestRailConfig().getString("password");						
						jiraDesc = "TestRail login: " + testRailUsername + " | " + testRailPassword;
					} else {
						//jiraDesc = testResultLink.replace("/", "\\/");
						jiraDesc = "See TestRail results below.";
					}								
					bugId = jira.postIssue(msg, jiraDesc);
					jiraLink = " - " + TestConfiguration.getJiraConfig().getString("host") + "/browse/" + bugId;
				}
				
				TestRailUtilities tr = new TestRailUtilities();
				tr.uploadResults(method, result, "JIRA bug - " + bugId + " : " + error, cloudTestJobIdLink);
				
				String testId = getTestId(BaseTest.projectId, BaseTest.suiteId, getCaseId(method));
				
				tr.getTestResults(testId);
				
				String testResultLink = TestConfiguration.getTestRailConfig().getString("host") + "/index.php?/tests/view/" + testId;
						
				if (testId == null) {
					testResultLink = "";
				}
				cn.postMsg(msg + " - " + testResultLink + jiraLink);
				
				/*
				 * alternative method to post jira issue using jmeter
				 *
				String jiraSummary = method.getName() + " - " + error;
				String jiraDesc; 
					
					if (cloudTest) {
						jiraDesc = msg + " - " + cloudTestJobIdLink.replace("/", "\\/");		
					} else {
						jiraDesc = msg + " - " + testResultLink.replace("/", "\\/");
					}	 
				TestRailUtilities tr = new TestRailUtilities();
				jiraMap.put(BaseTest.runId + "TESTRAIL" + tr.getCaseId(method) + "JIRA" + jiraSummary, jiraDesc);
				*/
				
			} else if (cloudTest) {
				cn.postMsg(msg + " - " + cloudTestJobIdLink);			
			}		
			failCount++;			
		}
		this.appendSkippedAndFailedTests(result, method);		
		this.removePassedTestsFromTestNG(result, method);
		try {
			if(Driver.getDriver() != null) {
				Driver.getDriver().quit();
			}
		} catch (Exception e) {}
		
		if (cloudTest) {
			SauceLabs sl = new SauceLabs();
			sl.createShellScriptUpdateResults(testResults);
			
			String[] cmd = new String[] {"bash", "shell scripts/updateSauceLabsResults.sh"};
			ExecuteShellCommand es = new ExecuteShellCommand();
			es.executeArrayCommand(cmd);
		}
		
		/*
		if (failCount == 100) {
			logger.error("100 tests failed. Exiting...");
			logger.info("not passed case ids: " + notPassedCaseIds.toString());
			System.exit(1);
		}
		*/
	}
	
	//@AfterClass(alwaysRun = true)
	public void afterClass() {
		if (updTestRail) {			
			Iterator<?> it = jiraMap.entrySet().iterator();
		    while (it.hasNext()) {
		        @SuppressWarnings("rawtypes")
				Map.Entry pair = (Map.Entry)it.next();
		        String summary = pair.getKey().toString();
		        String desc = pair.getValue().toString();
		        String assignee = TestConfiguration.getJiraConfig().getString("assignee"); 		        		
		        createJiraIssue(summary, desc, assignee);
		    }
		}
	}
	
	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		if (!cloudTest) {
			logger.info("see test-output/screenshots for failed tests\n");
		}
		this.writeSkippedAndFailedTests();
		if (updTestRail) {
			//this.closeRun(runId);
		}
		String testng = "src/test/resources/testng/testng_retryFailed.xml";
		ExecuteShellCommand es = new ExecuteShellCommand();
		String[] cmd1 = new String[] {"sed", "-i.tmp", "s/BUILD_TAG/"+runName+"/g", testng};
		String[] cmd2 = new String[] {"sed", "-i.tmp", "s/\\<include name\\=\\\"\\.\\*\\\" \\/\\>//g", testng};		
		String[] cmd3 = new String[] {"sed", "-i.tmp", "s/runId\" value=\"\"/runId\" value=\""+BaseTest.runId+"\"/g", testng};
		
		es.executeArrayCommand(cmd1);		
		es.executeArrayCommand(cmd2);
		es.executeArrayCommand(cmd3);
		removeTmpFiles();
		logger.info("not passed case ids: " + notPassedCaseIds.toString());
	}	
	
	public void appendSkippedAndFailedTests(ITestResult result, Method method) {
		if ((!sb.toString().contains(method.getName()) && result.getStatus() == ITestResult.FAILURE && Retry.retryCount == Retry.MAXRETRYCOUNT) || result.getStatus() == ITestResult.SKIP) {
			sb.append("<include name=\"" + method.getName() + "\" />\n");
			addFailedTestsToTestNG(method);
			notPassedCaseIds.append("c" + this.getCaseId(method) + ",");
		} 
	}	
	
	public void writeSkippedAndFailedTests() {		
        BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter("test-output/failedAndSkippedTests.txt"));
			output.write("<methods>\n" + sb.toString() + "</methods>");
	        output.close();
	        logger.info("list of failed and skipped test cases are saved in failedAndSkippedTests.txt. add to testng.xml to rerun only those test cases for debugging");
		} catch (IOException e) {
			logger.error("writeSkippedAndFailedTests");
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}        
	}
		
	public void addFailedTestsToTestNG(Method method) {
		String[] command = new String[] {"sed", "-i.tmp", "s/\\<include name\\=\\\"\\.\\*\\\" \\/\\>"
				+ "/\\<include name\\=\\\""+method.getName()+"\\\" \\/\\>"
				+ "\\\n\\<include name\\=\\\"\\.\\*\\\" \\/\\>/g", "src/test/resources/testng/testng_retryFailed.xml"};
		ExecuteShellCommand es = new ExecuteShellCommand();
		es.executeArrayCommand(command);
	}
	
	public void removePassedTestsFromTestNG(ITestResult result, Method method) {
		if (result.getStatus() == ITestResult.SUCCESS) {
			String[] command = new String[] {"sed", "-i.tmp", "s/\\<include name\\=\\\""+method.getName()+"\\\" \\/\\>//g", "testng_retryFailed.xml"};
			ExecuteShellCommand es = new ExecuteShellCommand();
			es.executeArrayCommand(command);
		}
	}
	
	public void createTestNGfailed(FileUtilities fu) {

		String testngDir = "src/test/resources/testng/";		
		File from = new File(testngDir + "testng.xml");		
		try {
			fu.deleteExistsFile(testngDir + "testng_retryFailed.xml");
			File to = new File(testngDir + "testng_retryFailed.xml");
			FileUtilities.copyFile(from, to);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createJiraIssue(String summary, String desc, String assignee) {
		
		String[] s1 = summary.split("TESTRAIL");
		String testRailRunId = s1[0];
		String[] s2 = s1[1].split("JIRA");
		String testRailcaseId = s2[0];
		summary = s2[1].replaceAll("[^a-zA-Z0-9]+"," ");
		
		String jmx = "src/test/jmeter/jira.jmx";
		ExecuteShellCommand es = new ExecuteShellCommand();
		es.executeCommand("cp " + jmx + " " + jmx.replace("jira", "jiraCopy"));
		
		String[] s3 = desc.split(" - ");
		String descWithoutLink = desc.replace(" - " + s3[s3.length - 1], "").replace(" - ", "_").replace(" ", "_");
		String testRailOrCloudTestLink = s3[s3.length - 1];
				
		String[] cmd = new String[] {"sed", "-i.tmp", "s/REPLACE_SEARCH/"+descWithoutLink+"/g", jmx};
		es.executeArrayCommand(cmd);
						
		String[] cmd1 = new String[] {"sed", "-i.tmp", "s/REPLACE_SUMMARY/"+descWithoutLink+"/g", jmx};
		es.executeArrayCommand(cmd1);
		
		String[] cmd2 = new String[] {"sed", "-i.tmp", "s/REPLACE_DESC/"+testRailOrCloudTestLink+"/g", jmx};
		es.executeArrayCommand(cmd2);
		
		String[] cmd3 = new String[] {"sed", "-i.tmp", "s/REPLACE_ASSIGNEE/"+assignee+"/g", jmx};
		es.executeArrayCommand(cmd3);
		
		String[] cmd4 = new String[] {"sed", "-i.tmp", "s/RUN_ID/"+testRailRunId+"/g", jmx};
		es.executeArrayCommand(cmd4);
		
		String[] cmd5 = new String[] {"sed", "-i.tmp", "s/CASE_ID/"+testRailcaseId+"/g", jmx};
		es.executeArrayCommand(cmd5);
		
		// enable all the tests
		String[] cmd6 = new String[] {"bash", "shell scripts/disable_jmeter_tests.sh", "*.jmx", "-xe"};
		es.executeArrayCommand(cmd6);
		
		// disable all the tests except for jira.jmx
		String[] cmd7 = new String[] {"bash", "shell scripts/disable_jmeter_tests.sh", "jira.jmx", "-x"};
		es.executeArrayCommand(cmd7);
		
		String jenkinsHome = System.getProperty("JENKINS_HOME");
		jenkinsHome = System.getProperty("JENKINS_HOME") != null ? jenkinsHome : System.getProperty("user.home") + "/.jenkins";
		
		String[] cmd8 = new String[] {jenkinsHome + "/tools/hudson.tasks.Maven_MavenInstallation/Maven/bin/mvn", "jmeter:jmeter"};
		es.executeArrayCommand(cmd8);
				
		es.executeCommand("cp " + jmx.replace("jira", "jiraCopy") + " " + jmx);
		
		String[] cmd9 = new String[] {"bash", "shell scripts/disable_jmeter_tests.sh", "*.jmx", "-xe"};
		es.executeArrayCommand(cmd9);
	}
	
	public void removeTmpFiles() {
		File jmeterDir = new File("src/test/jmeter");

		for (File f : jmeterDir.listFiles())
			if (f.getName().contains(".tmp") || f.getName().contains("Copy"))
				f.delete();
		
		File testngDir = new File("src/test/resources/testng");

		for (File f : testngDir.listFiles())
			if (f.getName().contains(".tmp"))
				f.delete();

	}
	
	public void skipSauceTestRunLocally() {
		if (BaseTest.cloudTest) {
			throw new SkipException("Need to run locally.");
		}
	}
	
	public static void skipBrowser(String browser) {
		if (BaseTest.browser.equals(browser)) {
			throw new SkipException("Skipping browser " + browser);
		}
	}
	
	public boolean isPerfTest(Method method) {
		Test testClass = method.getAnnotation(Test.class);

        for (int i = 0; i < testClass.groups().length; i++) {
            if (testClass.groups()[i].contains("Performance")) {
            	return true;
            }
        }
        return false;
	}
	
	public String getTestSuite() {
		ExecuteShellCommand es = new ExecuteShellCommand();		
		String[] cmd = new String[] {"grep", "-Ril", "public void " + BaseTest.getMethodName(), "src/test/java/com/automation/tests"};
		String suite = es.executeArrayCommand(cmd);
		suite = suite.replace(".java", "");
		return suite;
	}
}