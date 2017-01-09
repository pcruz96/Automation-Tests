package com.automation.data;

import com.automation.config.TestConfiguration;
import com.automation.tests.BaseTest;
import com.automation.utils.Log4J;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtilities extends Log4J {
	
	Connection con = null;
	Statement st = null;
	ResultSet rs = null;
	String result = null;
	String url = null;
	String user = null;
	String password = null;
	
	public void setConfig() {
		url = TestConfiguration.getDbConfig().getString(BaseTest.database + ".url");
		user = TestConfiguration.getDbConfig().getString(BaseTest.database + ".username");
		password = TestConfiguration.getDbConfig().getString(BaseTest.database + ".password");
	}
	
	public String executeQuery(String query) {				
		try {
			this.setConfig();
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				result = writeMetaData(rs);
				//logger.info(result);
			}
		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(DatabaseUtilities.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException ex) {
				Logger lgr = Logger
						.getLogger(DatabaseUtilities.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}		
		return result;
	}
	
	public List<String> getResults(String query) {

		List<String> data = new ArrayList<String>();

		try {
			this.setConfig();
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				result = writeMetaData(rs);
				data.add(result);
				//logger.info(result);
			}
		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(DatabaseUtilities.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException ex) {
				Logger lgr = Logger
						.getLogger(DatabaseUtilities.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}		
		return data;
	}

	private String writeMetaData(ResultSet resultSet) throws SQLException {
		// Now get some metadata from the database
		// Result set get the result of the SQL query

		String rs = "";
		Integer columnCount = resultSet.getMetaData().getColumnCount();
		//logger.info("The columns in the table are: ");
		//logger.info("Table: " + resultSet.getMetaData().getTableName(1));
		
		for (int i = 1; i <= columnCount; i++) {
			//logger.info("Column " +i+ " "+resultSet.getMetaData().getColumnName(i));			
			// logger.info(resultSet.getString(i));
			//rs += resultSet.getString(i) + " | ";
			if (columnCount == 1) {
				rs = resultSet.getString(i);	
			} else {
				rs += resultSet.getMetaData().getColumnName(i) + " = " + resultSet.getString(i) + "\n";
			}
		}
		return rs.toString();
	}
	
	public void updateRows(String update) {
		this.setConfig();
		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			st.executeUpdate(update);
			st.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	public String runQuery(String query, int sleep) {
		String result = null;
		
		for (int i = 1; i < 11; i++) {
			result = this.executeQuery(query);
			
			if (result == null) {
				try {
					//logger.info("retry db query count: " + i);
					Thread.sleep(sleep);					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			} else {
				logger.info(result);
				return result;
			}
		} 
		return null;
	}
}
