package com.gurock.testrail;

import com.automation.tests.BaseTest;
import com.automation.utils.Log4J;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GetTestCases extends Log4J {

	public static void main(String[] args) {
		
		logger.info("Args: projectId suiteId startTCID endTCID");
		if ((args == null) || (args.length == 0) || (args.length < 4)) {
			logger.info("Check args.\n"
					+ "Usage: projectId suiteId startTCID endTCID");
			System.exit(0);
		}
		String projectId = args[0];
		String suiteId = args[1];
		int startTCID = Integer.parseInt(args[2]);
		int endTCID = Integer.parseInt(args[3]);
		String filePath = "convertedTestRailTests_TestNGmethods.txt";
		TestRailUtilities tr = new TestRailUtilities();
		
		ArrayList<TestcaseModel> results = tr.getTestCases(projectId, suiteId, startTCID, endTCID);				
		String suiteName = tr.getSuiteName(false, suiteId).replace("Tests", "") + "Tests";
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("import org.testng.annotations.Test;\n\n");
			bw.write("public class " + suiteName + " extends BaseTest {\n\n");
			for (TestcaseModel t : results) {
				
				String sections = tr.getSections(t.getTestID());
				
				String testCaseMethodStr = "\t@Test(groups={\""+sections+", "+t.getType()+"\"}, enabled=false)\n"
						+ "\tpublic void c" + t.getTestID() + "_"
						+ getTestCaseName(t.getDescription())
						+ "() {\n" + "\t\n" + "\t}\n";
				
				logger.info("ADDED: " + testCaseMethodStr);
				
				bw.write(testCaseMethodStr + "\n");
			}
			bw.write("}");
			bw.close();
			logger.info("created convertedTestRailTests_TestNGmethods.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static String getTestCaseName(String testCaseName) {
		testCaseName = testCaseName.replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_");
		return testCaseName.toLowerCase();
	}
	
	public static String getAutomatedTestCaseSteps(String suite, String testCase) {

		String testCaseSteps = null;
		TestRailUtilities tr = new TestRailUtilities();
		String project;
		if (BaseTest.updTestRail) {
			project = tr.getProjectName(BaseTest.projectId).replace(" ", "_");
		} else {
			project = BaseTest.project;
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader("src/test/java/com/automation/tests/"+project+"/"+suite+".java"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			String group = null;
			boolean append = false;
			int lineNumber = 2; // package and the blank space before the first import
			while (line != null) {
				line = br.readLine();
				try {
					if (line.contains("@Test")) {
						group = lineNumber + " " + line;
					}
					if (append || line.contains("public void " + testCase)) {
						append = true;	
						if (line.contains(testCase)) {
							sb.append(group + "\n");
						}
						sb.append(lineNumber + " " + line + "\n");
					}
				} catch (Exception e) {

				}
				if (append && line.contains("}")) {					
					break;
				}
				lineNumber++;
			}
			testCaseSteps = "\n" + sb.toString();			
			return testCaseSteps;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testCaseSteps;
	}
	
	public static List<Integer> getAutomatedTests(String suite) {

		TestRailUtilities tr = new TestRailUtilities();
		String project;
		if (BaseTest.updTestRail) {
			project = tr.getProjectName(BaseTest.projectId).replace(" ", "_");
		} else {
			project = BaseTest.project;
		}
		
		List<Integer> caseIds = new ArrayList<Integer>();
		
		try (BufferedReader br = new BufferedReader(new FileReader("src/test/java/com/automation/tests/"+project+"/"+suite+".java"))) {
			
			String line = br.readLine();
			boolean foundTest = false;

			while (line != null) {
				line = br.readLine();
				try {
					if (line.contains("@Test") && line.replaceAll(" ", "").contains("enabled=true")) {
						foundTest = true;
					}
				} catch (Exception e) {}
				
				if (foundTest && !line.contains("@Test")) {
					
					String[] s1 = line.split(" ");
					String[] s2 = s1[2].split("_");
					Integer caseId = Integer.parseInt(s2[0].replace("c", ""));
					caseIds.add(caseId);
					foundTest = false;
				}				
			}			
			return caseIds;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return caseIds;
	}
}
