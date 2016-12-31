package com.gurock.testrail;

import com.automation.tests.BaseTest;
import com.automation.utils.FileUtilities;
import com.automation.utils.Log4J;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GetTestCases extends Log4J {

	public static void main(String[] args) {
		
		logger.info("Args: projectId suiteId startTCID endTCID");
		if ((args == null) || (args.length == 0) || (args.length < 3)) {
			logger.info("Check args.\n"
					+ "Usage: projectId suiteId startTCID endTCID");
			System.exit(0);
		}
		String projectId = args[0];
		String suiteId = args[1];
		int startTCID = Integer.parseInt(args[2]);
		int endTCID;
		try {
			endTCID = Integer.parseInt(args[3]);
		} catch (Exception e) {
			endTCID = startTCID;
		}
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
				
				String sections = null;
				do {
					sections = tr.getSections(t.getTestID());
				} while (sections == null);
				
				sections = sections.substring(0, sections.length() - 2);
				
				String testCaseMethodStr = "\t@Test(groups={\""+sections+"\"}, enabled=false)\n"
						+ "\tpublic void c" + t.getTestID() + "_"
						+ getTestCaseName(t.getDescription())
						+ "() {\n" + "\t\n" + "\t}\n";
				
				logger.info("ADDED: " + testCaseMethodStr);
				
				bw.write(testCaseMethodStr + "\n");
			}
			bw.write("}");
			bw.close();
			logger.info("refresh Automation-Tests dir to see convertedTestRailTests_TestNGmethods.txt");
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
	
	public String getAutomatedTestCaseSteps(String testCase) {

		String testCaseSteps = null;
		TestRailUtilities tr = new TestRailUtilities();
		String project;
		if (BaseTest.updTestRail) {
			project = tr.getProjectName(BaseTest.projectId).replace(" ", "_");
		} else {
			project = BaseTest.project;
		}
		
		FileUtilities fu = new FileUtilities();
		String suite = "";
		try {
			suite = fu.scanFiles("src/test/java/com/automation/tests/" + project, "public void " + testCase);			
		} catch (Exception e) {}
		
		try (BufferedReader br = new BufferedReader(new FileReader("src/test/java/com/automation/tests/"+project+"/"+suite))) {
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
				if (append && line.contains("}") && !line.contains("{")) {					
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
		
		String includeGroups = System.getenv("INCLUDEGROUPS");
		String excludeGroups = System.getenv("EXCLUDEGROUPS");
		
		try (BufferedReader br = new BufferedReader(new FileReader("src/test/java/com/automation/tests/"+project+"/"+suite+".java"))) {
			
			String line = br.readLine();
			boolean foundTest = false;

			while (line != null) {
				line = br.readLine();
				try {
					if (line.contains("@Test") && line.replaceAll(" ", "").contains("enabled=true")) {
						
						String[] g = null;
						
						if (includeGroups == null || includeGroups.equals(".*") && excludeGroups.isEmpty()) {
							
							foundTest = true;							
							
						} else if (includeGroups.equals(".*") && !excludeGroups.isEmpty()) {
							
							g = excludeGroups.split(",");																
							int i = 0;
							
							for (String group : g) {
								if (line.contains(group.trim())) {									
									break;
								} else if (i == g.length - 1) {
									foundTest = true;
								}
								i++;
							}
						
						} else {
							if (!includeGroups.equals(".*")) {
							
								g = includeGroups.split(",");
								
								for (String group : g) {
									if (line.contains(group.trim())) {
										foundTest = true;
										break;
									}
								}
							
							} else if (!excludeGroups.isEmpty()) {
								
								g = excludeGroups.split(",");
								
								for (String group : g) {
									if (!line.contains(group.trim())) {
										foundTest = true;
										break;
									}
								}							
							}
						}
					}
				} catch (Exception e) {}
				
				if (foundTest && !line.contains("@Test")) {
					
					String[] s1 = line.split(" ");
					String[] s2 = s1[2].split("_");
					Integer caseId = Integer.parseInt(s2[0].replace("c", ""));
					
					String cids = System.getenv("CASEIDS");
					
					if (cids != null) {
						for (String c : cids.split(",")) {
							if (c.replace("c", "").trim().equals(caseId.toString())) {
								caseIds.add(caseId);
								break;
							}
						}							
					} else {
						caseIds.add(caseId);	
					}						
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
