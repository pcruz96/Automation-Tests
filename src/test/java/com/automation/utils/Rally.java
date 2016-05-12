package com.automation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;

public class Rally {

	public final String host = "https://rally1.rallydev.com";
	final String username = "";
	final String password = "";
	final String workspaceRef = "/workspace/";
	
	public void convertRallyTestCasesToTestNGmethods(String project) throws URISyntaxException, IOException {
		
		String suiteName = project.replaceAll("[^a-zA-Z0-9]+","_");
		String filePath = "convertedRallyTestCasesToTestNGmethods.java";		
		String projectRef = "/project/" + getProjectRef(project);

		RallyRestApi restApi = null;
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("import org.testng.annotations.Test;\n\n");
			bw.write("public class " + suiteName + " extends BaseTest {\n\n");
			
			restApi = new RallyRestApi(new URI(host), username, password);

			QueryRequest testSetRequest = new QueryRequest("TestCases");
			testSetRequest.setWorkspace(workspaceRef);
			testSetRequest.setProject(projectRef);
			testSetRequest.setFetch(new Fetch(new String[] { "Name", "_ref", "Type" }));

			QueryResponse testSetQueryResponse = restApi.query(testSetRequest);
			System.out.println("Successful: " + testSetQueryResponse.wasSuccessful());					
			System.out.println("Size: " + testSetQueryResponse.getTotalResultCount());
					
			for (int i = 0; i < testSetQueryResponse.getResults().size(); i++) {
				JsonObject testSetJsonObject = testSetQueryResponse.getResults().get(i).getAsJsonObject();
				String[] s = testSetJsonObject.get("_ref").getAsString().split("testcase/");
				
				String id = s[1].replace(".js", "");
				String methodName = id + "_" + testSetJsonObject.get("_refObjectName").getAsString().replaceAll("[^a-zA-Z0-9]+"," ").replace(" ", "_").toLowerCase();
				String group = testSetJsonObject.get("Type").getAsString();				
				System.out.println(methodName);
				
				String testCaseMethodStr = "\t@Test(groups={\""+group+"\"}, enabled=false)\n"
						+ "\tpublic void c" + methodName
						+ "() {\n" + "\t\n" + "\t}\n";
				bw.write(testCaseMethodStr + "\n");
			}
			bw.write("}");
			bw.close();
		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
	}
	
	public void getTestCases(String project) throws URISyntaxException, IOException {
		
		String projectRef = "/project/" + getProjectRef(project);

		RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(host), username, password);

			QueryRequest testSetRequest = new QueryRequest("TestCases");
			testSetRequest.setWorkspace(workspaceRef);
			testSetRequest.setProject(projectRef);
			testSetRequest.setFetch(new Fetch(new String[] { "Name", "_ref" }));

			QueryResponse testSetQueryResponse = restApi.query(testSetRequest);
			System.out.println("Successful: " + testSetQueryResponse.wasSuccessful());					
			System.out.println("Size: " + testSetQueryResponse.getTotalResultCount());
					
			for (int i = 0; i < testSetQueryResponse.getResults().size(); i++) {
				JsonObject testSetJsonObject = testSetQueryResponse.getResults().get(i).getAsJsonObject();
				String[] s = testSetJsonObject.get("_ref").getAsString().split("testcase/");
						
				String id = s[1].replace(".js", "");
				System.out.println(id + "_" + testSetJsonObject.get("_refObjectName").getAsString().replace(" ", "_"));
			}

		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
	}
	
	public String getLatestTestCaseResultId(String project, String testCaseId) throws URISyntaxException, IOException {
		
		String projectRef = "/project/" + getProjectRef(project);

		RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(host), username, password);

			QueryRequest testSetRequest = new QueryRequest("TestCaseResults");
			testSetRequest.setWorkspace(workspaceRef);
			testSetRequest.setProject(projectRef);
			testSetRequest.setFetch(new Fetch(new String[] { "Name", "TestCase", "_ref" }));

			QueryResponse testSetQueryResponse = restApi.query(testSetRequest);
					
			for (int i = testSetQueryResponse.getResults().size() - 1; i > 0; i--) {
				JsonObject testSetJsonObject = testSetQueryResponse.getResults().get(i).getAsJsonObject();				
						
				if (testSetJsonObject.toString().contains("testcase/" + testCaseId)) {					
					String[] s = testSetJsonObject.get("_ref").toString().split("/");
					return s[s.length - 1].replace("\"", "");
				}				
			}

		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
		return null;
	}
	
	public String getProjectRef(String project) throws URISyntaxException, IOException {
		
		RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(host), username, password);

			QueryRequest testSetRequest = new QueryRequest("Projects");
			testSetRequest.setWorkspace(workspaceRef);
			testSetRequest.setFetch(new Fetch(new String[] { "Name", "_ref" }));

			QueryResponse testSetQueryResponse = restApi.query(testSetRequest);
			for (int i = 0; i < testSetQueryResponse.getResults().size(); i++) {
				JsonObject testSetJsonObject = testSetQueryResponse.getResults().get(i).getAsJsonObject();						

				//System.out.println(testSetJsonObject.get("_refObjectName").getAsString() + " - " + testSetJsonObject.get("_ref"));
				
				String _refObjectName = testSetJsonObject.get("_refObjectName").getAsString().replaceAll("[^a-zA-Z0-9]+","_") + "Tests";
				
				if (_refObjectName.equals(project)) {
						
					String[] s = testSetJsonObject.get("_ref").getAsString().split("project/");
					return s[1];
				}				
			}
		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
		return null;
	}
	
	public String getAutomatedTestCaseSteps(String suite, String testCase) {

		String testCaseSteps = null;
				
		try (BufferedReader br = new BufferedReader(new FileReader("src/test/java/com/automation/tests/"+suite+".java"))) {
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
							sb.append(group + "<br/>");
						}
						sb.append(lineNumber + " " + line + "<br/>");
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

	public void createTestResult(String build, String testCaseId, String verdict, String notes) throws URISyntaxException, IOException {			

		RallyRestApi restApi = new RallyRestApi(new URI(host), username, password);

		JsonObject json = new JsonObject();
		json.addProperty("Method", "Automated");
		UpdateRequest updateRequest = new UpdateRequest("/testcase/" + testCaseId, json);				
		restApi.update(updateRequest);

		String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date());				

		JsonObject newResult = new JsonObject();
		newResult.addProperty("TestCase", "/testcase/" + testCaseId);
		newResult.addProperty("Build", build);
		newResult.addProperty("Date", date);
		newResult.addProperty("Verdict", verdict);
		newResult.addProperty("Notes", notes);

		CreateRequest createRequest = new CreateRequest("testcaseresult", newResult);				
		restApi.create(createRequest);
		restApi.close();
	}
	
	public String getLink(String link) {
		return "<a href=\""+link+"\">"+link+"<a/>";
	}
	
	public String getTestCaseId(String testCase) {
		String[] s = testCase.split("_");		
		return s[0].replace("c", "");
	}
}
