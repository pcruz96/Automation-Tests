
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
	String assignee = TestConfiguration.getJiraConfig().getString("assignee");
		
	public String postIssue(String summary, String desc) {
		try {
			Client client = Client.create();			
			client.addFilter(new HTTPBasicAuthFilter(username, password));
			WebResource webResource = client.resource(host + "/rest/api/2/issue");
			
			summary = summary.replaceAll("[^a-zA-Z0-9]+"," ").replace(" ", "_");			
			
			String input="{\"fields\":{\"project\":{\"key\":\""+key+"\"},\"summary\":\""+summary+"\",\"description\":\""+desc+"\", \"assignee\":{\"name\":\""+assignee+"\"}, \"issuetype\":{\"name\":\"Bug\"}}}";		
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
			
			summary = summary.replaceAll("[^a-zA-Z0-9]+"," ").replace(" ", "_");
			
			WebResource webResource = client.resource(host + "/rest/api/2/search?jql=summary~"+summary+"&fields=summary,status," + resolutionField);					
			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
			String output = response.getEntity(String.class);
			String[] s = output.split("customfield_11401");

			if (output.indexOf("total\":0") > 0 || s[1].indexOf("\"Fixed\"") > 0 || s[1].indexOf("\"Verified\"") > 0 || s[1].indexOf("\"Closed\"") > 0 || s[1].indexOf("\"Done\"") > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getKey(String summary) {
		try {
			Client client = Client.create();			
			client.addFilter(new HTTPBasicAuthFilter(username, password));
			
			summary = summary.replaceAll("[^a-zA-Z0-9]+"," ").replace(" ", "_");
			
			WebResource webResource = client.resource(host + "/rest/api/2/search?jql=summary~"+summary+"&fields=summary");					
			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
			String output = response.getEntity(String.class);
			
			if (output.indexOf("total\":0") > 0) {
				return null;
			}
			String[] s1 = output.split("key");
			String[] s2 = s1[1].split(",");
			String key = s2[0].replace("\"", "").replace(":", "");
			return key;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeIssue(String summary, String sauceLabsJobIdLink) {
		try {
			String key = this.getKey(summary);
			
			if (key != null) {
				Client client = Client.create();			
				client.addFilter(new HTTPBasicAuthFilter(username, password));
				WebResource webResource = client.resource(host + "/rest/api/2/issue/"+key+"/transitions?expand=transitions.fields");
				/*
				transitions:
					11 = Start Development
					21 = Resolved
					31 = Verified
					41 = Close
				*/
				for (int i=1; i < 5; i++) {				
					try {
						String input="{\"update\":{\"comment\":[{\"add\":{\"body\":\"automation passed "+sauceLabsJobIdLink+"\"}}]},\"transition\":{\"id\":\""+i+"1\"}}";		
						ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
						String output = response.getEntity(String.class);
						//System.out.println(output);
					} catch (Exception e) {}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
