#! /bin/bash
java -jar src/test/resources/selenium/selenium-server-standalone-2.53.0.jar -role hub | \
java -jar -Dwebdriver.chrome.driver="src/test/resources/selenium//chromedriver" src/test/resources/selenium/selenium-server-standalone-2.53.0.jar -role node -hub http://localhost:4444/grid/register \

#-maxSession 25 -browser browserName=chrome,maxInstances=25

