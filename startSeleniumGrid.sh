#! /bin/bash
java -jar ext/selenium-server-standalone-2.46.0.jar -role hub | \
java -jar ext/selenium-server-standalone-2.46.0.jar -role node -hub http://localhost:4444/grid/register \
-maxSession 25 -browser browserName=chrome,maxInstances=25

