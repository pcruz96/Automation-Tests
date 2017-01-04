package com.automation.testng;

import java.io.*;
import java.util.HashSet;

public class AddTestNGclasses {

	static HashSet<String> testMethodsClasses;
	static String testDir = "";

	@SuppressWarnings("resource")
	public static void main(String[] arg) {

		String inputFile = "src/test/resources/testng/testng.xml";
		String outputFile = "src/test/resources/testng/testng_groups.xml";
		File ifile = new File(inputFile);
		File ofile = new File(outputFile);
		testDir = arg[0];

		if (System.getenv("CASEIDS") != null) {
			testMethodsClasses = getTestMethodsClasses();
			disableTests();
			deleteTmpFiles();
			enableTests();
			deleteTmpFiles();
		}

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
					if (!line.contains("CLASS")) {
						bw.write(line + "\n");
					}
					if (line.contains("<classes>") && i == 0) {

						String[] dirs = executeCommand("find " + testDir + " -type d").split("\n");

						for (String dir : dirs) {

							boolean subDir = false;

							if (!dir.equals(testDir)) {
								subDir = true;
							}

							if (!dir.contains("archived")) {

								String[] tests = executeCommand("ls " + dir).split("\n");

								for (String test : tests) {
									if (test.contains(".java")) {
										if (System.getenv("CASEIDS") == null
												|| (System.getenv("CASEIDS") != null && testMethodsClasses.toString().contains("<" + test + ">"))) {
											String pkg = dir.replace("src/test/java/", "").replace("/", ".");

											if (subDir) {
												pkg += ".";
											}
											String className = "<class name=\"" + pkg.replace("..", ".")
													+ test.replace(".java", "") + "\" />" + "\n";
											bw.write(className);
										}
									}
								}
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();
	}

	public static HashSet<String> getTestMethodsClasses() {
		HashSet<String> hs = new HashSet<String>();
		String suite = "";
		String[] dirs = executeCommand("find " + testDir + " -type d").split("\n");
		String[] tests = System.getenv("CASEIDS").split(",");

		for (String dir : dirs) {
			for (String test : tests) {
				suite = scanFiles(dir, "public void " + test + "_");
				if (suite != null) {
					hs.add("<" + suite + ">");
				}
			}
		}
		return hs;
	}

	public static String scanFiles(String folderPath, String searchString) {
		try {
			File folder = new File(folderPath);

			if (folder.isDirectory()) {
				for (File file : folder.listFiles()) {
					if (!file.isDirectory()) {
						BufferedReader br = new BufferedReader(new FileReader(file));
						String content = "";
						try {
							StringBuilder sb = new StringBuilder();
							String line = br.readLine();

							while (line != null) {
								sb.append(line);
								sb.append(System.lineSeparator());
								line = br.readLine();
							}
							content = sb.toString();

						} finally {
							br.close();
						}
						if (content.contains(searchString)) {
							return file.getName().toString();
						}
					}
				}
			} else {
				System.out.println("Not a Directory!");
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String executeArrayCommand(String[] command) {
		 
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
	
	public static void disableTests() {
		String[] dirs = executeCommand("find src/test/java/com/automation/tests/ -type d").split("\n");
		String[] sed;		
		for (String dir : dirs) {
			
			String[] tests = executeCommand("ls " + dir).split("\n");
			
			for (String test : tests) {
				sed = new String[] {"sed", "-i.tmp", "s/enabled=true/enabled=false/g", dir + "/" + test};
				executeArrayCommand(sed);
			}
		}
	}
	
	public static void enableTests() {
		String suite = "";
		String[] dirs = executeCommand("find " + testDir + " -type d").split("\n");
		String[] tests = System.getenv("CASEIDS").split(",");
		File file = new File("src/test/java/com/automation/testng/sedCmds.txt");
		FileWriter fw;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String dir : dirs) {
			for (String test : tests) {
				String searchMethod = "public void " + test + "_";
				suite = scanFiles(dir, searchMethod);
				if (suite != null) {
					String suitePath = dir + "/" + suite;
					String[] grep = executeCommand("grep -n " + test + " " + suitePath).split("\n");
					
					for (String result : grep) {
						
						if (result.contains(searchMethod)) {
							
							String[] r = result.split(":");
							int testLineNum = Integer.valueOf(r[0]);
							int enabledLineNum = testLineNum - 1;
							try {						
								bw.write("sed -i.tmp " + enabledLineNum + "s/enabled=false/enabled=true/ " + suitePath + "\n");
								break;
							} catch (IOException e) {			
								e.printStackTrace();
							}		
						}
					}					
				}
			}
		}	
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		executeCommand("bash " + file);
		file.delete();
	}
	
	public static void deleteTmpFiles(){		
		String[] dirs = executeCommand("find src/test/java/com/automation/tests/ -type d").split("\n");

		for (String dir : dirs) {			
			File d = new File(dir);
			File[] files = d.listFiles();
	        for(File test : files) {
	        	if (test.toString().contains(".tmp")) {
	        		test.delete();
	        	}
	        }
		}
	}
}
