package com.automation.utils;
 
import org.apache.log4j.Logger;
 
public class Log4J{
 
	public static final Logger logger = Logger.getLogger(Log4J.class);
 
	public static void main(String[] args) {
 
		Log4J obj = new Log4J();
		obj.runMe("mkyong");

	}
 
	private void runMe(String parameter){
 
		if(logger.isDebugEnabled()){
			logger.debug("This is debug : " + parameter);
		}
 
		if(logger.isInfoEnabled()){
			logger.info("This is info : " + parameter);
		}
 
		logger.warn("This is warn : " + parameter);
		logger.error("This is error : " + parameter);
		logger.fatal("This is fatal : " + parameter);
 
	} 
}
