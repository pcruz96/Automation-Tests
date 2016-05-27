#! /bin/bash
echo $BUILD_TAG
sed -i.tmp "s/BUILD_TAG/$BUILD_TAG/g" src/test/resources/testng/testng.xml
