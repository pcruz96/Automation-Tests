package com.automation.testng;

import java.io.*;

public class AddTestNGmethods {

	@SuppressWarnings("resource")
	public static void main(String[] arg) {

		String inputFile = "src/test/resources/testng/testng.xml";
		String outputFile = "src/test/resources/testng/testng_groups.xml";
		File ifile = new File(inputFile);
		File ofile = new File(outputFile);
		String[] caseIds = arg[0].split(",");
		AddTestNGmethods.writeCaseIds("remainingTestRunCaseIds.txt", arg[0]);

		try {
			if (!ofile.exists()) {
				ofile.createNewFile();
			}
			FileWriter fw = new FileWriter(ofile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();
			bw.write(line + "\n");		
			int i = 0;
			

			while (line != null) {
				line = br.readLine();
				try {
					if (!line.contains(".*") ) {
						bw.write(line + "\n");											
					}					
					if (line.contains("<methods>") && i == 0) {
						for (String caseId : caseIds) {
							caseId += "_.*";
							bw.write("<include name=\".*"+caseId.replace(",", "").trim()+"\" />" + "\n");							
						}
						i++;
					}					
					if (line.contains("</suite>")) {
						break;
					}
				} catch (Exception e) {
				}
			}
			bw.close();			
			ofile.renameTo(ifile);
			System.out.println("updated " + inputFile);
		} catch (IOException e) {
		}
	}
	
	public static void writeCaseIds(String fileName, String caseIds) {
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(caseIds.replace(" ", ""));
			bufferedWriter.close();
		} catch (IOException ex) {
			System.out.println("Error writing to file '" + fileName + "'");
		}
	}
}
