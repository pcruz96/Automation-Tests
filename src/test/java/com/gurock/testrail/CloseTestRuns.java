package com.gurock.testrail;

import com.automation.utils.Log4J;

public class CloseTestRuns extends Log4J {

	public static void main(String[] args) {
		logger.info("Usage: projectId containsTxt");
		if ((args == null) || (args.length == 0) || (args.length < 2)) {
			logger.info("Empty Args.\n"
					+ "Usage: projectId containsTxt");
			System.exit(0);
		}
		TestRailUtilities t = new TestRailUtilities();
		String projectId = args[0];
		String txt = args[1];
		t.closeRunsContainingTxt(projectId, txt);
	}
}
