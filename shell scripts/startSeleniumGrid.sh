#! /bin/bash
jarPath="../src/test/resources/selenium/selenium-server-standalone-2.53.0.jar"

java -jar $jarPath -role hub | \
java -jar -Dwebdriver.chrome.driver="../src/test/resources/selenium/chromedriver.exe" $jarPath -role node -hub http://localhost:4444/grid/register \
-maxSession 25 -browser browserName=phantomjs,maxInstances=25

