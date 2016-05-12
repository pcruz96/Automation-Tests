package com.automation.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;

public class CreateExcelExecutiveTestReport {
	
	static Workbook wb;
	static Row header;
	static Sheet sheet;
	static boolean showDetails;
	static boolean colorTotals;
	static boolean automationOnly;
	static short colAutoNum = 0;
	static short colStepsNum = 0;
	static short colErrorsNum = 0; 
	static short colTitleNum = 0;	
	static boolean oneRow = false;

	public static void main(String[] args) throws Exception {
		
		if ((args == null) || (args.length == 0) || (args.length < 3)) {
			System.out.println("Empty Args.\n"
					+ "Usage: booleanShowDetails booleanColorTotals booleanAutomationOnly inputFile outputFile");
			System.exit(0);
		}
		InputStream inp = null;
		showDetails = Boolean.valueOf(args[0]);
		colorTotals = Boolean.valueOf(args[1]); 
		automationOnly = Boolean.valueOf(args[2]);
		String inputFile = args[3];		
		String outputFile = args[4];		
		inp = new FileInputStream(inputFile);

		wb = WorkbookFactory.create(inp);
		sheet = wb.getSheetAt(0);
		sheet.setDefaultColumnWidth(30);
		header = sheet.getRow(0);
				
		formatHeaderAndTotals(header, 0, 0, 0, "");
		
		int lastRow = sheet.getLastRowNum();
		sheet.createRow(lastRow + 1);
		sheet.createFreezePane(2, 1);		
		short colManualNum = 0;
		short colTotalNum = 0;
		short colSectionNum = 0;
		short colPassedNum = 0;
		short colFailedNum = 0;
		short colUntestedNum = 0;		
		short colTestNum = 0;
		int groupStartRow = 1;
		String colTitleLetter = null;
		String colAutoLetter = null;
		String colManualLetter = null;
		String colTotalLetter = null;
		String colPassedLetter = null;
		String colFailedLetter = null;
		String colUntestedLetter = null;
		String nxtCellValue = null;
		String sectionRange = null;
		String status = null;
		Row firstRow = null; 

		for (Row row : sheet) {						
			for (Cell cell : row) {
				CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());						
				String cellValue = null;
				int currentRow = row.getRowNum();

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:

					cellValue = cell.getStringCellValue();

					switch (cellValue) {
					case "ID":
						firstRow = row;
						colTestNum = cellRef.getCol();
						
						if (showDetails != true) {
							sheet.setColumnHidden(cellRef.getCol(), true);
						}
						break;
					case "Title": //Section						
						colTitleNum = cellRef.getCol();
						colTitleLetter = cellRef.formatAsString().substring(0, 1);
						cell.setCellValue("Section");
						break;						
					case "Type": //Total
						colTotalNum = cellRef.getCol();						
						cell.setCellValue("Total");
						break;						
					case "Priority": //Automated
						colAutoNum = cellRef.getCol();
						colAutoLetter = cellRef.formatAsString().substring(0, 1);
						createColumn(getColumn("Total"), colAutoNum);
						cell.setCellValue("Automated");
						break;								
					case "Estimate": //Manual
						colManualNum = cellRef.getCol();
						colManualLetter = cellRef.formatAsString().substring(0, 1);
						createColumn(getColumn("Automated"), colManualNum);
						cell.setCellValue("Manual");
						break;
					case "Status": //Passed
						colPassedNum = cellRef.getCol();
						colPassedLetter = cellRef.formatAsString().substring(0,1);								
						cell.setCellValue("Passed");
						break;
					case "Assigned To": //Failed 
						colFailedNum = cellRef.getCol();
						colFailedLetter = cellRef.formatAsString().substring(0,1);
						createColumn(getColumn("Passed"), colFailedNum);
						cell.setCellValue("Failed");
						break;					
					case "In Progress": //Untested
						colUntestedNum = cellRef.getCol();
						colUntestedLetter = cellRef.formatAsString().substring(0,1);
						createColumn(getColumn("Passed"), colUntestedNum);
						cell.setCellValue("Untested");
						break;
					case "Section":
						colSectionNum = cellRef.getCol();						
						break;					
					case "Case ID": case "Steps": case "References": case "Test Run":
						
						if (cellValue.matches("Steps")) {
							colStepsNum = cellRef.getCol();
							
						} else if (cellValue.matches("References")) {
							colErrorsNum = cellRef.getCol();
							cell.setCellValue("Errors");
						}
								
						if (showDetails != true) {
							sheet.setColumnHidden(cellRef.getCol(), true);
						}
						break;
					
					case "Milestone":					
					case "Preconditions":					
					case "Expected Result":
					case "Defects": 
					case "Depth":
					case "Elapsed":					
					case "Last Update":					
					case "Configuration":
						sheet.setColumnHidden(cellRef.getCol(), true);
						break;
					}
					
					if (row.getRowNum() > 0 && cellRef.getCol() == colPassedNum) {
						status = cell.getStringCellValue();
					}						
					
					if (currentRow > 0 && cellRef.getCol() == colSectionNum) {
						Row nxtRow = sheet.getRow(currentRow + 1);
						Cell nxtRowCell = nxtRow.getCell(cellRef.getCol());

						try {
							nxtCellValue = nxtRowCell.getStringCellValue();
						} catch (Exception e) {
							nxtCellValue = null;
						}

						String section = cell.getStringCellValue();
						
						try {
							Row updRow = sheet.getRow(currentRow - 1);																						
							formatTitle(updRow, status, colTitleNum);
							String testResult = updRow.getCell(colPassedNum).getStringCellValue();
							updateErrors(updRow, testResult);
						} catch (Exception e) {}
												
						// different section
						if (cellValue != nxtCellValue) {
							
							//group section and collapse rows
								
								int groupEndRow = currentRow;
								sheet.groupRow(groupStartRow, groupEndRow);
								sheet.setRowGroupCollapsed(groupStartRow, true);

							//create row at the end of the section to display totals
								
								sheet.createRow(currentRow);
								Row rowTotals = sheet.getRow(currentRow);
								for (int i = 0; i < row.getLastCellNum(); i++) {
									rowTotals.createCell(i);
								}
								
							Cell cellSectionTotals = rowTotals.getCell(1);
							cellSectionTotals.setCellType(Cell.CELL_TYPE_STRING);									
							cellSectionTotals.setCellValue(section);

							Cell cellSection = rowTotals.getCell(colSectionNum);
							cellSection.setCellType(Cell.CELL_TYPE_STRING);
							cellSection.setCellValue(nxtCellValue);

							//ID column
							
								Cell cellid = rowTotals.getCell(colTestNum);
								cellid.setCellType(Cell.CELL_TYPE_STRING);
								cellid.setCellValue("Expand section details");									

							//Automated column
								
								Cell cellType = rowTotals.getCell(colAutoNum);
								cellType.setCellType(Cell.CELL_TYPE_FORMULA);								
								sectionRange = colAutoLetter + (groupStartRow + 1) + ":" + colAutoLetter+ (groupEndRow);								
								String autoSection = "COUNTIF(" + sectionRange + ", \"Automated\")";
								String manualSection = "COUNTIF(" + sectionRange + ", \"Other\")";
								String rowSection = autoSection + "+" + manualSection;
								String autoPercent = "ROUND("+autoSection+"/("+rowSection+")*100,0)&\"%";								
								cellType.setCellFormula(autoSection+alignEqualsInColumn(groupStartRow, groupEndRow, colAutoNum, "Automated")+autoPercent+"\"");
																
							//Manual column
								
								Cell cellManual = rowTotals.getCell(colManualNum);
								cellManual.setCellType(Cell.CELL_TYPE_FORMULA);
								sectionRange = colManualLetter + (groupStartRow + 1) + ":" + colManualLetter+ (groupEndRow);
								autoSection = "COUNTIF(" + sectionRange + ", \"Automated\")";
								manualSection = "COUNTIF(" + sectionRange + ", \"Other\")";
								rowSection = autoSection + "+" + manualSection;																							
								String manualPercent = "ROUND("+manualSection+"/("+rowSection+")*100,0)&\"%";								
								cellManual.setCellFormula(manualSection+alignEqualsInColumn(groupStartRow, groupEndRow, colManualNum, "Other")+manualPercent+"\"");
								
							//Total column
								
								Cell cellTotal = rowTotals.getCell(colTotalNum);
								cellTotal.setCellType(Cell.CELL_TYPE_FORMULA);
								sectionRange = colManualLetter + (groupStartRow + 1) + ":" + colManualLetter+ (groupEndRow);
								String total;
								if (groupStartRow == groupEndRow) {
									total = "1";
								} else {
									total = rowSection;
								}								
								cellTotal.setCellFormula(total);								
															
							//Passed column
								
								Cell cellPassed = rowTotals.getCell(colPassedNum);
								cellPassed.setCellType(Cell.CELL_TYPE_FORMULA);	
								if (groupStartRow + 1 == groupEndRow) {
									groupStartRow--; //fix #VALUE!
									oneRow = true;
								}	
								sectionRange = colPassedLetter + (groupStartRow + 1) + ":" + colPassedLetter + (groupEndRow);
								String rowsSection = "ROWS("+sectionRange+")";
								String passedSection = "COUNTIF(" + sectionRange + ", \"Passed\")"; 
								String sectionPassPercent = null;		
								sectionPassPercent = formulaCellRange(groupStartRow, groupEndRow, passedSection, rowsSection);
								cellPassed.setCellFormula(passedSection+alignEqualsInColumn(groupStartRow, groupEndRow, colPassedNum, "Passed")+sectionPassPercent+"\"");
								
							//Failed column

								Cell cellFailed = rowTotals.getCell(colFailedNum);
								cellFailed.setCellType(Cell.CELL_TYPE_FORMULA);								
								sectionRange = colFailedLetter + (groupStartRow + 1) + ":" + colFailedLetter + (groupEndRow);
								rowsSection = "ROWS("+sectionRange+")"; 
								String failedSection = "COUNTIF(" + sectionRange + ", \"Failed\")";
								String sectionFailPercent = formulaCellRange(groupStartRow, groupEndRow, failedSection, rowsSection);								
								cellFailed.setCellFormula(failedSection+alignEqualsInColumn(groupStartRow, groupEndRow, colFailedNum, "Failed")+sectionFailPercent+"\"");
								
							//Untested column
								
								Cell cellUntested = rowTotals.getCell(colUntestedNum);
								cellUntested.setCellType(Cell.CELL_TYPE_FORMULA);								
								sectionRange = colUntestedLetter + (groupStartRow + 1) + ":" + colUntestedLetter + (groupEndRow);
								rowsSection = "ROWS("+sectionRange+")"; 
								String untestedSection = "COUNTIF(" + sectionRange + ", \"Untested\")";
								String sectionUntestedPercent = formulaCellRange(groupStartRow, groupEndRow, untestedSection, rowsSection);	
								cellUntested.setCellFormula(untestedSection+alignEqualsInColumn(groupStartRow, groupEndRow, colUntestedNum, "Untested")+sectionUntestedPercent+"\"");
									
							formatHeaderAndTotals(rowTotals, colPassedNum, groupStartRow, groupEndRow - 1, "Passed");
							groupStartRow = currentRow + 1;
							oneRow = false;
						}							
					}					
				}				
			}
		}		
		
		// Grand totals
		
		sheet.setColumnHidden(colSectionNum, true);
		
		Row grandTotalsRow = sheet.getRow(sheet.getLastRowNum());
		
		for (int i = 0; i < 100; i++) {
			grandTotalsRow.createCell(i);
		}		
		
		grandTotalsRow.getCell(1).setCellValue("Grand total %");
		
		String idRange = "A2:A"+(lastRow + 1);
		String sections = "COUNTIF("+idRange+", \"Expand section details\")";
		
		String typeRange = colAutoLetter+"2:"+colAutoLetter+lastRow;
		String automated = "COUNTIF("+typeRange+", \"Automated\")";				
		String manual = "COUNTIF("+typeRange+", \"Other\")";
		
		String statusRange = colPassedLetter+"2:"+colPassedLetter+lastRow;
		String passed = "COUNTIF("+statusRange+",\"Passed\")";
		String failed = "COUNTIF("+statusRange+",\"Failed\")";
		String untested = "COUNTIF("+statusRange+",\"Untested\")";
		
		String formulaTotalRows = automated+"+"+manual;
		
		String autoPercent = "\"            \"&ROUND("+automated+"/("+formulaTotalRows+")*100,0)&\"%\"";
		String manualPassPercent = "\"            \"&ROUND("+manual+"/("+formulaTotalRows+")*100,0)&\"%\"";
		
		String totalPassPercent = "\"            \"&ROUND("+passed+"/("+formulaTotalRows+")*100,0)&\"%\"";
		String totalFailPercent = "\"            \"&ROUND("+failed+"/("+formulaTotalRows+")*100,0)&\"%\"";
		String totalUntestedPercent = "\"            \"&ROUND("+untested+"/("+formulaTotalRows+")*100,0)&\"%\"";
		
		firstRow.getCell(colTotalNum).setCellFormula("\"Total - \"&"+formulaTotalRows);

		//Total Automated column
		
			firstRow.getCell(colAutoNum).setCellFormula(automated + "&\" Automated\"");
			Cell cellAuto = grandTotalsRow.getCell(colAutoNum);					
			cellAuto.setCellFormula(autoPercent);
			
		//Total Manual column
			
			firstRow.getCell(colManualNum).setCellFormula("\"Manual - \"&"+manual);
			Cell cellManual = grandTotalsRow.getCell(colManualNum);
			cellManual.setCellFormula(manualPassPercent);
			
		//Section column
			
			Cell cellSection = sheet.getRow(0).getCell(colTitleNum);
			cellSection.setCellFormula(sections + "&\" Sections\"");
			
		//Passed column

			firstRow.getCell(colPassedNum).setCellFormula(passed + "&\" Passed\"");
			Cell cellPassed = grandTotalsRow.getCell(colPassedNum);
			cellPassed.setCellFormula(totalPassPercent);

		//Failed column

			firstRow.getCell(colFailedNum).setCellFormula(failed + "&\" Failed\"");
			Cell cellFailed = grandTotalsRow.getCell(colFailedNum);
			cellFailed.setCellFormula(totalFailPercent);

		//Untested column

			firstRow.getCell(colUntestedNum).setCellFormula(untested + "&\" Untested\"");
			Cell cellUntested = grandTotalsRow.getCell(colUntestedNum);
			cellUntested.setCellFormula(totalUntestedPercent);
				
		formatHeaderAndTotals(grandTotalsRow, 0, 0, 0, "");
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+colUntestedLetter+"1"));
		
		if (automationOnly) {
			sheet.setColumnHidden(colTotalNum, true);
			sheet.setColumnHidden(colManualNum, true);
		}
		
		// set active cell
	    wb.setActiveSheet(0);
	    sheet.showInPane((short)0, (short)0);
		
		FileOutputStream fileOut = null;
		fileOut = new FileOutputStream(outputFile);		
		wb.write(fileOut);
		fileOut.close();
		System.out.println("created "+outputFile);
	}

	public static void formatHeaderAndTotals(Row row, int col, int startRow, int endRow, String txt) {
		
		int percentNum = 0;		
		
		if (txt != null && txt != "") {		
			int count = 0;
			int rows = endRow - startRow + 1;
			
			for (int i = startRow; i <= endRow; i++) {
				Cell cell = sheet.getRow(i).getCell(col);
				if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
					String cellTxt = cell.getStringCellValue().trim();

					if (cellTxt.matches(txt)) {
						count++;
					}	
				}							
			}
			NumberFormat percentFormat = NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(0);
			String percent = percentFormat.format((double) count/(double) rows);
			percentNum = Integer.parseInt(percent.replace("%",""));
		}
					
		CellStyle style = wb.createCellStyle();// Create style
		Font font = wb.createFont();// Create font
		font.setFontHeightInPoints((short) 14);		
		style.setFont(font);						
		row.setHeight((short) 500);
		style.setFillBackgroundColor(IndexedColors.GREY_80_PERCENT.getIndex());					
		style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
		style.setFillPattern(HSSFCellStyle.BIG_SPOTS);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);	
		style.setAlignment(CellStyle.ALIGN_LEFT);		

		if (row.getRowNum() == 0) {
			
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);// Make font bold
			style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
			style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

		} else if (row.getRowNum() == wb.getSheetAt(0).getLastRowNum()) {

			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);// Make font bold
			style.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			
		} else if (colorTotals && startRow > 0) {
			
			if (percentNum > 80 || (oneRow && percentNum == 50)) {			
				font.setColor(IndexedColors.BRIGHT_GREEN.getIndex());
			} else if (percentNum < 50) {
				//font.setColor(IndexedColors.RED.getIndex());
				font.setColor(IndexedColors.WHITE.getIndex());
			} else {
				font.setColor(IndexedColors.ORANGE.getIndex());
			}	
			style.setFillBackgroundColor(IndexedColors.GREY_80_PERCENT.getIndex());					
			style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
			row.getCell(col).setCellStyle(style);
		} else {
			font.setColor(IndexedColors.WHITE.getIndex());			
		}
		
		for (int i = 0; i < header.getLastCellNum(); i++) {

			try {
				if (col == colStepsNum || col == colErrorsNum) {
					style.setWrapText(true);	
				}								
				row.getCell(i).setCellStyle(style);
			} catch (Exception e) {}
		}
	}
	
	public static void formatTitle(Row row, String status, int col) {
		
		row.getCell(2).getStringCellValue();
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);		
		
		if (status.equals("Passed")) {
			
			font.setColor(IndexedColors.BRIGHT_GREEN.getIndex());

		} else if (status.equals("Failed")) {

			font.setColor(IndexedColors.RED.getIndex());					
		}	
		row.getCell(colTitleNum).setCellStyle(style);
		createSauceLabsHyperlink(style, font, row);
	}
	
	public static void createSauceLabsHyperlink(CellStyle style, Font font2, Row row) {
		
		Cell title = row.getCell(colTitleNum);
		Cell steps = row.getCell(colStepsNum);
		String stepsValue = steps.getStringCellValue();
		
		if (stepsValue.contains("https")) {
			String[] s = stepsValue.split("https");
			String sauceLabsSession = null;
			if (s[1].contains("jenkins")) {
				String[] s2 = s[1].split("\n");
				sauceLabsSession = "https" + s2[0];
			} else {
				sauceLabsSession = "https" + s[1];	
			}
			font2.setUnderline(XSSFFont.U_SINGLE);
			style.setFont(font2);
			CreationHelper createHelper = wb.getCreationHelper();
			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
			link.setAddress(sauceLabsSession);
			title.setHyperlink(link);
			title.setCellStyle(style);
		}
	}
	
	public static String getColumn(String columnWanted) throws FileNotFoundException {

		Sheet sheet = wb.getSheetAt(0);
		Integer columnNo = null;
		List<Cell> cells = new ArrayList<Cell>();
		Row firstRow = sheet.getRow(0);

		for (Cell cell : firstRow) {
			if (cell.getStringCellValue().equals(columnWanted)) {
				columnNo = cell.getColumnIndex();
			}
		}

		if (columnNo != null) {
			for (Row row : sheet) {
				Cell c = row.getCell(columnNo);
				if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
					// Nothing in the cell in this row, skip it
				} else {
					cells.add(c);
				}
			}
			return cells.toString();
		} else {
			System.out.println("could not find column " + columnWanted + " in first row");						
		}
		return null;
	}
	
	public static void createColumn(String column, int colNum) {
		
		String[] s = column.split(",");

		for (int i = 1; i < s.length; i++) {
			String cellValue = s[i].replace("]", "");
			//System.out.println(cellValue);
			Row row = sheet.getRow(i);
			Cell cellSection = row.getCell(colNum);
			if (cellSection == null) {
				cellSection = row.createCell((short) colNum);
			}
			cellSection.setCellType(Cell.CELL_TYPE_STRING);
			cellSection.setCellValue(cellValue.trim());
		}
	}
	
	public static String formulaCellRange(int startRow, int endRow, String countIfSection, String rowsSection) {
		String formula = null;
		if (startRow == endRow) {
			formula = countIfSection+"/1*100&\"%";
		} else if (oneRow) {
			formula = countIfSection+"/2*200&\"%";
		} else {
			formula = "ROUND("+countIfSection+"/("+rowsSection+")*100,0)&\"%";
		}
		return formula;
	}	
	
	public static String alignEqualsInColumn(int startRow, int endRow, int col, String txt) {
		
		int count = 0;
		
		for (int i = startRow; i <= endRow; i++) {
			Cell cell = sheet.getRow(i).getCell(col);
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				String cellTxt = cell.getStringCellValue().trim();

				if (cellTxt.matches(txt)) {
					count++;
				}	
			}										
		}
		
		String equals = null;
		if (count < 10) {
			equals = "&\"     =   \"&";
		} else {
			equals = "&\"   =   \"&";
		}
		return equals;
	}
	
	public static void updateErrors(Row row, String testResult) {		
		if (!testResult.matches("Failed")) {
			try {
				row.getCell(colErrorsNum).setCellValue("");
			} catch (Exception e) {}
		}
	}
}
