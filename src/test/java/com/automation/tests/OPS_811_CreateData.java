package com.automation.tests.mercatus_application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class OPS_811_CreateData {

	public static void main(String[] args) throws IOException {
		
		Calendar calendar = Calendar.getInstance();
		
		for (int aid = 18147; aid < 18149; aid++) {
			
			String file = "foresight/" + aid + ".xls";
			FileOutputStream fos = new FileOutputStream(file);
			@SuppressWarnings("resource")
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("main");
						
			Row headerRow = sheet.createRow(0);			
			
			String headers = "asset_id,day,month,year,Irradiation_Actual,Production_Actual,Availability_Actual,Wind_Speed_Actual,Load_Factor_Actual,Feedstock_Actual,Production_Actual_HalfHourly,Irradiation_BaseCase,Production_BaseCase,Availability_BaseCase,Wind_Speed_BaseCase,Load_Factor_BaseCase,Feedstock_BaseCase,Irradiation_DebtBudget,Production_DebtBudget,Availability_DebtBudget,Wind_Speed_DebtBudget,Env_Gross_Power_Gen_Actual,Env_Net_Power_Exp_Actual,Env_Guaranteed_Power_Out_Actual,Env_MWh_FiT_Actual,Env_MWh_FiT_Budget,Env_MWh_RHI_Actual,Env_MWh_RHI_Budget,Env_MWh_ROCs_Actual,Env_MWh_ROCs_Budget,Env_Heat_Gen_Actual,Env_Heat_Export_Actual,Env_Feedstock_Del_Actual,Env_Feedstock_Process_Actual,Env_CH4_Actual,Env_Dry_Matter_Actual,Env_H2SBefore_Actual,Env_H2SAfter_Actual,Env_Pri_Digester_Actual,Env_Sec_Digester_Actual,Env_FOS/TAC_Pri_Actual,Env_FOS/TAC_Sec_Actual,Env_Pri_Digester_Level_Actual,Env_Sec_Digester_Level_Actual,Env_Digestate_Storage_Level_Actual,Env_Turbo_Level_Actual,Env_CH4_Prod_Actual,Env_Ferr_OH_Actual,Env_Ferr_Cl_Actual,Env_Biogas_Feed_Actual,Env_CO2_Actual,Env_Biogas_Prod_Actual,Env_Biomethane_Actual,Env_Flare_Actual,Env_CO2_Prod_Actual,Env_Propane_Actual,Env_Methane_Slip_Actual,Env_Feedstock_Dispatch_Actual,Env_Total_Recovery_Actual,Env_Total_Recycling_Actual,Env_Total_Treatment_Actual,Env_Total_Landfill_Actual,Env_Downtime_Actual,Env_Notes_Actual,Env_Operator_Feedback_Actual,Employment_Temp_Actual,Employment_Perm_Actual,Employment_Perm_Operation_Actual,Baseline_GHG_Actual,Absolute_GHG_Actual,GHG_Saved_Actual,Conventional_Gen_Actual,Conventional_Prod_Actual,Renewable_Gen_Actual,Renewable_Prod_Actual,Elec_EE_Actual,Thermal_EE_Actual,Health_Safety_Actual,Incidents_Actual,Near_Misses_Actual,RIDDOR_Events_Actual,Lost_Time_Incidents_Actual,Employment_Temp_Budget,Employment_Perm_Budget,Baseline_GHG_Budget,Absolute_GHG_Budget,GHG_Saved_Budget,Conventional_Gen_Budget,Conventional_Prod_Budget,Renewable_Gen_Budget,Renewable_Prod_Budget,Elec_EE_Budget,Thermal_EE_Budget";
			
			String[] hds = headers.split(",");
			int cell = 0;
			
			for (String h : hds) {				
				
				headerRow.createCell(cell).setCellValue(h);				
				cell++;								
			}			
			
			int rowNum = 1;
									
			for (int y = 2018; y < 2034; y++) {
				
				for (int m = 0; m < 12; m++) {
					
					calendar.set(Calendar.YEAR, y);
					calendar.set(Calendar.MONTH, m);
					int numDays = calendar.getActualMaximum(Calendar.DATE);
					
					for (int d = 1; d < numDays + 1; d++) {
						
						Row dayRow = sheet.createRow(rowNum);
						
						dayRow.createCell(0).setCellValue(aid);
						dayRow.createCell(1).setCellValue(d);
						dayRow.createCell(2).setCellValue((m + 1));
						dayRow.createCell(3).setCellValue(y);
						
						for (int c = 4; c < hds.length - 1; c++) {
							
							dayRow.createCell(c).setCellValue("1234567890");				
						}
						rowNum++;
					}										
				}
			}
			workbook.write(fos);
			fos.close();	        
	        
	        File inputFile = new File(file);
            File outputFile = new File(file.replace("xls", "csv"));
            xls(inputFile, outputFile);
            
            File f = new File(file);
            f.delete();
            System.out.println("finished " + aid);
		}
	}
	
	public static void xls(File inputFile, File outputFile) {
		// For storing data into CSV files
		StringBuffer data = new StringBuffer();
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);

			// Get the workbook object for XLS file
			@SuppressWarnings("resource")
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(inputFile));
			// Get first sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);
			Cell cell;
			Row row;

			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					cell = cellIterator.next();

					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN:
						data.append(cell.getBooleanCellValue() + ",");
						break;

					case Cell.CELL_TYPE_NUMERIC:
						data.append(cell.getNumericCellValue() + ",");
						break;

					case Cell.CELL_TYPE_STRING:
						data.append(cell.getStringCellValue() + ",");
						break;

					case Cell.CELL_TYPE_BLANK:
						data.append("" + ",");
						break;

					default:
						data.append(cell + ",");
					}

				}
				data.append('\n');
			}

			fos.write(data.toString().getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
