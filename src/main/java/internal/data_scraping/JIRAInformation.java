package main.java.internal.data_scraping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import main.java.internal.utils.JSONConfig;
import main.java.internal.entities.jira_entities.JIRAContent;

import com.google.gson.Gson;



public class JIRAInformation {

	private static JIRAInformation instance = null;


	private final List<JIRAContent> jiraTicketPages = new ArrayList<>();


	public static JIRAInformation getInstance() {
		if (instance == null) {
			instance = new JIRAInformation();
		}
		return instance;
	}

	private JIRAInformation() {
		super();
	}

	private void retrieveTickets() throws IOException {
		// we need to retrieve the encoded access token from the
		// configuration file
		String project = JSONConfig.getProjectName();
		String type = JSONConfig.getIssueType();
		int i = 0;
		int j;
		int total;
		Gson gson = new Gson();
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs
			// >1000
			j = i + 1000;
			String url = JSONConfig.getJiraApi() + "search?jql=project=%22" + project + "%22AND%22issueType%22=%22"
					+ type + "%22AND(%22status%22=%22closed%22OR"
					+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
					+ i + "&maxResults=" + j;
			String jsonStr = getJsonFromURL(url);
			JIRAContent currJiraContent = gson.fromJson(jsonStr, JIRAContent.class);
			this.jiraTicketPages.add(currJiraContent);
			total = currJiraContent.getTotal();
			i = j;
		} while (i < total);

	}

	private String getJsonFromURL(String url) throws IOException {
		// String building ..
		StringBuilder builder = new StringBuilder();
		InputStream is = new URL(url).openStream();
		InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				builder.append(inputLine);
				builder.append(System.lineSeparator());
			}
		}
		// return a string containing the page content
		return builder.toString().trim();

	}

	public List<JIRAContent> getJiraTickets() throws IOException {
		if (this.jiraTicketPages.isEmpty()) {
			retrieveTickets();
		}
		return this.jiraTicketPages;
	}

}
