#! /bin/bash
echo $SUITE
sed -i.tmp "s/BUILD_TAG/$SUITE/g" $BUILD_NUMBER/artifact/src/test/resources/testng/testng.xml
sed -i.tmp "s/BUILD_TAG/$SUITE/g" $BUILD_NUMBER/artifact/target/surefire-reports/emailable-report.html
