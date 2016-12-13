package com.automation.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

public class SparkNotifications {

	public void postMsg(String txt) {
		HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 

	    try { 
	        HttpPost request = new HttpPost("https://api.ciscospark.com/v1/messages");
	        //automation room
	        StringEntity params =new StringEntity("{\"roomId\": \"e59809b0-d047-11e5-ab9f-852061566ec3\", \"text\": \""+txt+"\"}");	        
	        request.addHeader(new BasicHeader("Authorization", "Bearer MWJiMDg2NDQtOTBlMy00OTc5LThhZTgtNDQxMDliM2FhMjNhM2U0Yjk1ODctY2Zl"));       
	        request.addHeader("content-type", "application/json");
	        request.setEntity(params);
	        httpClient.execute(request);
	        //System.out.println(response.toString());
	    } catch (Exception ex) {}	    
	}
}
