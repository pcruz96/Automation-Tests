package com.automation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
	
	private static final String FILE_NAME = "src/test/resources/testData/TestData.xlsx";

	public static String getCellValue(String sheet, int row, int col) throws IOException {
		FileInputStream fi = new FileInputStream(new File(FILE_NAME));
	    @SuppressWarnings("resource")
		Workbook wb = new XSSFWorkbook(fi);
	    Sheet sh = wb.getSheet(sheet);
	    Cell c = sh.getRow(row).getCell(col);
	    return c.getStringCellValue();
	}
}
