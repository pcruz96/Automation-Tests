package com.automation.tests;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONObject;

import com.automation.utils.ExecuteShellCommand;
import com.google.gson.JsonObject;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Test {
	
	static int sum = 0;
	
	static void printFib() {
		
		int[] ia = new int[15];
		ia[0] = 0;
		ia[1] = 1;
		
		for (int i=2; i < ia.length; i++) {
			ia[i] = ia[i-1] + ia[i-2];
		}
		
		for (int i=0; i < ia.length; i++) {
			System.out.println(ia[i] + " ");
		}
	}
	
	static void sortArray() {
		int[] ia = {9,4,4,4,5,6,7,8};
		//String[] ia = new String[] {"abb","aa"};
		
		Arrays.sort(ia);
		
		for (Integer n : ia) {
			System.out.println(n);	
		}
	}
	
	static void getArrayDup() {
		int[] ia = {9,4,4,4,5,6,7,8};
		//String[] ia = new String[] {"abb","aa"};
				
		Map<Integer, Integer> hm = new HashMap<Integer, Integer>();
		 
		// (2) print the java int array
		for (int i = 0; i < ia.length; i++) {
			try {
				if (!hm.containsKey(ia[i])) {
					hm.put(ia[i], 1);
				} else {
					hm.put(ia[i], hm.get(ia[i]) + 1);
				}
			} catch (Exception e) {
			}
		}
		
		for (Integer key : hm.keySet()) {
		    Integer value = hm.get(key);
		    
		    if (value > 1) {
				System.out.println(key + " - " + value);
			}
		}
	}
	
	static boolean isPrime(int num) {
		
		for (int i=2; i <= num/2; i++) {
			if (num % i == 0) {
				return false;
			}
		}		
		return true;
	}
	
	static int reverseNum(int num) {
		
		int revNum = 0;
		
		while(num != 0) {
			revNum = (revNum*10) + (num%10);
			num = num/10;
		}		
		return revNum; 
	}
	
	static int getNumberSum(int number){
        
        if(number == 0){
            return sum;
        } else {
            sum += (number%10);
            
            System.out.println((number%10));
            
            getNumberSum(number/10);
        }
        return sum;
    }
	
	static void printDistinctElements(int[] arr){
        
        for(int i=0;i<arr.length;i++){
            boolean isDistinct = false;
            for(int j=0;j<i;j++){
                if(arr[i] == arr[j]){
                    isDistinct = true;
                    break;
                }
            }
            if(!isDistinct){
                System.out.print(arr[i]+" ");
            }
        }
    }
	
	static String removeDups(String str) {
		return str.replaceAll("\\(+", "(").replaceAll("\\)+", ")").replaceAll("\\($", "");
	}
	
	static String getSimpleDir(String dir) {
		String simpleDir = null;
		
		String[] s = dir.split("/");
		ArrayList<String> al = new ArrayList<String>();
		
		for (int i = 0; i < s.length; i++) {
			
			if (s[i].matches("..")) {				
				al.remove(al.size() - 1);
			} else {
				al.add(s[i]);	
			}				
		}	
		
		StringBuilder sb = new StringBuilder(); 
		for (String st : al) {
			if (!st.isEmpty())
				sb.append("/" + st);
		}		
		return sb.toString();
	}
	
	public static ArrayList<Integer> getAry() {
		
		ArrayList<Integer> al = new ArrayList<Integer>();
		int[] ia = {1,7,3,4};
		int v;
		
		for (int i=0; i<ia.length; i++) {
			v = 1;
			for (int j=0; j<ia.length; j++) {
				if (i != j) {
					v *= ia[j];
				}
			}
			al.add(v);
		}		
		return al;
	}	

	public static void main(String[] args) throws URISyntaxException, IOException {
		
		String[] cmd = new String[] {"sed", "-i.tmp", "s/REPLACE_SUMMARY/test summary/g", "src/test/jmeter/jira.jmx"};
		ExecuteShellCommand es = new ExecuteShellCommand();
		es.executeArrayCommand(cmd);
		
		String[] cmd2 = new String[] {"sed", "-i.tmp", "s/REPLACE_DESC/test desc/g", "src/test/jmeter/jira.jmx"};
		es.executeArrayCommand(cmd2);
		
		String[] cmd3 = new String[] {"bash", "/Users/pcruz/Jmeter/apache-jmeter-2.13/bin/jmeter.sh", "-n", "-t", "src/test/jmeter/jira.jmx"};
		es.executeArrayCommand(cmd3);
		
		//System.out.println(removeDups("((a))(b))"));
		//System.out.println(getSimpleDir("/a/b/../../c/../d"));
		//System.out.println(getAry());
		/*
		 * 
		
		int number = 2;
        int count = 0;
        int sum = 0;
        while(count < 1000){
            if(isPrime(number)){
                sum += number;
                count++;
            }
            number++;
        }
        System.out.println(sum);

        
		int[] nums = {5,2,7,2,4,7,8,2,3};
        printDistinctElements(nums);        
		System.out.println("sum of 123: " + getNumberSum(123));
		System.out.println("reverse # of 123: " + reverseNum(123));
		System.out.println("is 5 prime?" + isPrime(5));		
		getArrayDup();
		sortArray();
		printFib();
		*/
	}
}
