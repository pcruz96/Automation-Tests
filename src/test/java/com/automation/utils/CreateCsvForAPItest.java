package com.automation.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gurock.testrail.TestRailUtilities;
import com.gurock.testrail.TestcaseModel;

import au.com.bytecode.opencsv.CSVWriter;

public class CreateCsvForAPItest {

	public static void main(String[] args) throws IOException {
		
		System.out.println("Args: projectId suiteId startTCID endTCID");
		if ((args == null) || (args.length == 0) || (args.length < 4)) {
			System.out.println("Check args.\n"
					+ "Usage: projectId suiteId startTCID endTCID");
			System.exit(0);
		}
		String projectId = args[0];
		String suiteId = args[1];
		String tests = args[2];
		String[] tc = tests.split(",");
		
		TestRailUtilities tr = new TestRailUtilities();
		ArrayList<TestcaseModel> results = tr.getTestCases(projectId, suiteId, tc);
		
		CSVWriter writer = new CSVWriter(new FileWriter("api.tests.csv"), ',');
		
		writer.writeNext("enabled,case_id,scenario,method,http_request,body_data,expected_response_code,response_contains_txt,comment".split(","));
		List<String[]> row = new ArrayList<String[]>();
		
		for (TestcaseModel t : results) {
			row.add(new String[] {"false", t.getTestID(), t.getDescription()});			
		}		
		writer.writeAll(row);
		writer.close();
		System.out.println("refresh Automation-Tests dir to see api.tests.csv");
	}
}
