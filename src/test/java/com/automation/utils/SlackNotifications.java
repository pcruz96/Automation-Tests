package com.automation.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class SlackNotifications {
	
	//demoTest-group/autotest

	public void postMsg(String txt) {
		HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 

	    try {
	    	// demotest-group.slack.com
	        HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
	        StringEntity params =new StringEntity("payload={\"channel\": \"#automation\", \"username\": \"webhookbot\", \"text\": \""+txt+"\"}");
	        request.addHeader("content-type", "application/x-www-form-urlencoded");
	        request.setEntity(params);
	        httpClient.execute(request);
	    }catch (Exception ex) {}	    
	}
}
