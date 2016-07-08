package com.automation.testng;

import java.io.*;

public class AddFailedTestsToTestNG {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		String inputFile = "src/test/resources/testng/testng_retryFailed.xml";
		String outputFile = "src/test/resources/testng/testng.xml";
		File ofile = new File(outputFile);

		try {
			if (!ofile.exists()) {
				ofile.createNewFile();
			}
			FileWriter fw = new FileWriter(ofile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();
			bw.write(line + "\n");
			boolean passedRunTag = false;
			boolean wroteFailedTests = false;

			while (line != null) {
				line = br.readLine();
				try {
					if (!line.contains("methods>") && !line.contains("<include name=\".*\" />")) {
						
						bw.write(line + "\n");
						
						if (line.contains("</run>")) {
							passedRunTag = true;
						}
					} else {
						if (passedRunTag && !wroteFailedTests) {
							BufferedReader br2 = new BufferedReader(new FileReader("test-output/failedAndSkippedTests.txt"));
							
							String line2 = br2.readLine();
	
							while (line2 != null) {							
								bw.write(line2 + "\n");
								line2 = br2.readLine();
							}
							wroteFailedTests = true;
						}
					}
					if (line.contains("</suite>")) {
						break;
					}
				} catch (Exception e) {
				}
			}
			bw.close();			
			System.out.println("updated " + outputFile);
		} catch (IOException e) {
		}
	}
}
