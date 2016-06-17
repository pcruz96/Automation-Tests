package com.automation.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
 
public class StackTraceToStringExample {
       
        public static void main(String args[]){
               
                try{
                       
                        //this will throw NumberFormatException
                        Integer.parseInt("Not a number");
                       
                }catch(NumberFormatException e){
                       
                        /*
                         * To convert Stacktrace to String in Java, use
                         * printStackTrace(PrintWrite pw) method of Throwable
                         * class.
                         */
                       
                        //create new StringWriter object
                        StringWriter sWriter = new StringWriter();
                       
                        //create PrintWriter for StringWriter
                        PrintWriter pWriter = new PrintWriter(sWriter);
                       
                        //now print the stacktrace to PrintWriter we just created
                        e.printStackTrace(pWriter);
                       
                        //use toString method to get stacktrace to String from StringWriter object
                        String strStackTrace = sWriter.toString();
                       
                        System.out.println("Stacktrace converted to String: " + strStackTrace);
                }
        }
       
}