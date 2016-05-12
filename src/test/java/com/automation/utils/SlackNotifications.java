package com.automation.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class SlackNotifications {

	public void postMsg(String txt) {
		HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 

	    try {
	        HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
	        StringEntity params =new StringEntity("payload={\"channel\": \"#automation\", \"username\": \"webhookbot\", \"text\": \""+txt+"\"}");
	        request.addHeader("content-type", "application/x-www-form-urlencoded");
	        request.setEntity(params);
	        HttpResponse response = httpClient.execute(request);
	    }catch (Exception ex) {}	    
	}
}
