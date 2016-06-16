# Automation-Tests

Getting started:

Create a workspace dir and cd into it.

To clone the repo from the cmd line: 

	git clone https://github.com/Mercatus/Automation-Tests
	
I recommend installing SourceTree, a GUI for Git repos.
	
	https://www.sourcetreeapp.com/	
	
In Eclipse, File > New > Java Project. Enter the name of the repo for the Project name so that it's in synch with Github.

![Alt text](readmeScreenshots/create java project.png?raw=true)

Copy dependencies from pom.xml to your local box. In the pom.xml, temporarily comment out the suiteXmlFile. 

	cd Automation-Tests
	mvn dependency:copy-dependencies -Dclassifer=sources
	cp target/dependencies Automation-Tests 

In Eclipse, Help > Install New Software for TestNG:

![Alt text](readmeScreenshots/install testng.png?raw=true)

In Eclipse, configure Java Build Path:

![Alt text](readmeScreenshots/java build path source.png?raw=true)

![Alt text](readmeScreenshots/libraries.png?raw=true)

Convert TestRail test cases to TestNG methods

![Alt text](readmeScreenshots/get test cases.png?raw=true)

See examples of Page Objects, Tests, and Workflows as a guideline:

	Automation-Tests/src/test/java/com/automation/pageObjs
	Automation-Tests/src/test/java/com/automation/tests
	Automation-Tests/src/test/java/com/automation/workflows

Configure testng_mercatus.xml

![Alt text](readmeScreenshots/config testng.png?raw=true)

From the cmd line, launch the Selenium Grid from the Automation-Tests dir:

	bash shell\ scripts/startSeleniumGrid.sh

Run testng_mercatus.xml

![Alt text](readmeScreenshots/run testng.png?raw=true)

After the test run, refresh Automation-Tests dir to see the results under Automation-Tests/test-output