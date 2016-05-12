package com.gurock.testrail;

import com.automation.tests.BaseTest;
import com.automation.utils.Log4J;

import java.io.*;
import java.util.ArrayList;

public class GetTestCases extends Log4J {

	public static void main(String[] args) {
		
		logger.info("Usage: projectId suiteId textFilePath");
		if ((args == null) || (args.length == 0) || (args.length < 3)) {
			logger.info("Empty Args.\n"
					+ "Usage: projectId suiteId textFilePath");
			System.exit(0);
		}
		logger.info("projectId " + args[0]);
		logger.info("suiteId " + args[1]);
		logger.info("testfile " + args[2]);
		String projectId = args[0];
		String suiteId = args[1];
		String filePath = args[2];
		TestRailUtilities testRailUtilities = new TestRailUtilities();

		ArrayList<TestcaseModel> results = testRailUtilities.getTestCases(
				projectId, suiteId);
		String suiteName = testRailUtilities.getSuiteName(false, suiteId);
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
				String testCaseMethodStr = "\t@Test(enabled=false)\n"
						+ "\tpublic void c" + t.getTestID() + "_"
						+ getTestCaseName(t.getDescription())
						+ "() {\n" + "\t\n" + "\t}\n";
				bw.write(testCaseMethodStr + "\n");
			}
			bw.write("}");
			bw.close();
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
		
		try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/com/automation/tests/"+project+"/"+suite+".java"))) {
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
}
