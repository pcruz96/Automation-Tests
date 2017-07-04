package com.automation.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CompareDates {

	public static String compareDates(String dateFormat, String date1, String date2) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date d1 = sdf.parse(date1);
        Date d2 = sdf.parse(date2);

        String result = null;

        if (d1.compareTo(d2) > 0) {
        	result = "date1 is after date2";
        } else if (d1.compareTo(d2) < 0) {
        	result = "date1 is before date2";
        } else if (d1.compareTo(d2) == 0) {
        	result = "date1 is equal to date2";
        }
        return result;
    }
}
