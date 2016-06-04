
package com.automation.utils;

import java.util.Map;

import com.automation.config.TestConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.media.jfxmedia.logging.Logger;

public class Jira {
	
	String host = TestConfiguration.getJiraConfig().getString("host");
	String username = TestConfiguration.getJiraConfig().getString("username");
	String password = TestConfiguration.getJiraConfig().getString("password");
	String key = TestConfiguration.getJiraConfig().getString("key");
	String resolutionField = TestConfiguration.getJiraConfig().getString("resolutionField");
		
	public String postIssue(String summary, String desc) {
		try {
			Client client = Client.create();			
			client.addFilter(new HTTPBasicAuthFilter(username, password));
			WebResource webResource = client.resource(host + "/rest/api/2/issue");
			
			summary = summary.replaceAll("[^a-zA-Z0-9]+"," ").replace(" ", "_");
			
			String input="{\"fields\":{\"project\":{\"key\":\""+key+"\"},\"summary\":\""+summary+"\",\"description\":\""+desc+"\", \"issuetype\":{\"name\":\"Bug\"}}}";		
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
	
			String output = response.getEntity(String.class);

			String[] s = output.split(",");
			String issue = s[1].replaceAll("\"", "").replaceAll("key:", "");
			
			System.out.println("Output from Server .... \n");
			System.out.println(output);
			return issue;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isNewIssueOrResolutionIsFixedOrStatusIsVerifiedOrClosed(String summary) {
		try {
			Client client = Client.create();			
			client.addFilter(new HTTPBasicAuthFilter(username, password));
			WebResource webResource = client.resource(host + "/rest/api/2/search?jql=summary~"+summary+"&fields=summary,status," + resolutionField);					
			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
			String output = response.getEntity(String.class);
			String[] s = output.split("customfield_11401");

			if (output.indexOf("total\":0") > 0 || s[1].indexOf("\"Fixed\"") > 0 || s[1].indexOf("\"Verified\"") > 0 || s[1].indexOf("\"Closed\"") > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
