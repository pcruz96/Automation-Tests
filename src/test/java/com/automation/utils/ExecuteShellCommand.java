package com.automation.utils;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
 
public class ExecuteShellCommand {
 
	public static void main(String[] args) {
 
		ExecuteShellCommand obj = new ExecuteShellCommand();
 
		String domainName = "google.com";
 
		//in mac oxs
		String command = "ping -c 3 " + domainName;
 
		//in windows
		//String command = "ping -n 3 " + domainName;
 
		String output = obj.executeCommand(command);
		System.out.println(output);
	}
 
	public String executeCommand(String command) {
 
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
	
	public String executeArrayCommand(String[] command) {
		 
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
