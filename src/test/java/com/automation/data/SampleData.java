package com.automation.data;

import java.lang.reflect.Method;

import org.testng.annotations.DataProvider;

public class SampleData {

    @DataProvider(parallel = true, name = "Data")
    public static Object[][] data(Method mtd) {

        Object detail[][] = null;

        if (mtd.getName().equalsIgnoreCase("verifyDataDriven")) {

            detail = new Object[2][1];
            detail[0][0] = "name1";
            detail[1][0] = "name2";
        }

        else if (mtd.getName().equalsIgnoreCase("verifyBusinessTypeValue")) {

        	detail = new Object[2][1];
            detail[0][0] = "Government";
            detail[1][0] = "Healthcare";
        }
        return detail;
    }
}
