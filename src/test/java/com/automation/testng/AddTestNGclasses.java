package com.automation.testng;

import java.io.*;

public class AddTestNGclasses {

	@SuppressWarnings("resource")
	public static void main(String[] arg) {

		String inputFile = "src/test/resources/testng/testng.xml";
		String outputFile = "src/test/resources/testng/testng_groups.xml";
		File ifile = new File(inputFile);
		File ofile = new File(outputFile);
				
		String[] tests = executeCommand("ls " + arg[0]).split("\n");
		String pkg = arg[0].replace("src/test/java/", "").replace("/", ".");

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
					if (!line.contains("CLASS") ) {
						bw.write(line + "\n");											
					}					
					if (line.contains("<classes>") && i == 0) {
						for (String test : tests) {
							if (test.contains(".java")) {
								bw.write("<class name=\""+pkg+test.replace(".java", "")+"\" />" + "\n");
							}
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
	
	public static String executeCommand(String command) {
		 
		StringBuffer output = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString(); 
	}
}
