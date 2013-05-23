package com.sap.demo.search.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

	private DBUtil dbAccess;

	public DBManager(DBUtil dbAccess) {
		this.dbAccess = dbAccess;
	}
	
	public void initDB() throws IOException {
		dropTables();
		createTables();
		createIndexes();
		insertTables();
		createProcedure();
	}
	
	public List<List<String>> getSearchResultRows(String searchText, String[] COLUMN_NAMES) {
		List<List<String>> result = new ArrayList<List<String>>();
		Connection connection = dbAccess.getConnection();
		
		try {
			CallableStatement preparedCall = connection.prepareCall("call findProduct(?,?)");
			preparedCall.setString(1, searchText);
			preparedCall.execute();
			ResultSet resultSet = preparedCall.getResultSet();
			
			while (resultSet.next()) {
				ArrayList<String> columnValues = new ArrayList<String>();
				result.add(columnValues);
				
				for (String columnName : COLUMN_NAMES) {
					columnValues.add(resultSet.getString(columnName));
				}
			}
			
		} catch (SQLException e) {
			throw new RuntimeException("Error calling 'findProduct' procedure.", e);
		} finally {
			dbAccess.closeConnection(connection);
		}
		return result;
	}

	private void createProcedure() throws IOException {
		try {
			dbAccess.executeBatch(dbAccess.mergeToString(loadResource("create_procedure.sql")));
		} catch (SQLException e) {
			throw new RuntimeException("Can not create procedure:\n", e);
		}
	}

	private void insertTables() throws IOException {
		try {
			dbAccess.executeBatch(loadResource("insert_tables.sql"));
		} catch (SQLException e) {
			throw new RuntimeException("Can not insert tables:\n", e);
		}
	}

	private void createIndexes() throws IOException {
		try {
			dbAccess.executeBatch(loadResource("create_indexes.sql"));
		} catch (SQLException e) {
			throw new RuntimeException("Can not create indexes:\n", e);
		}
	}

	private void createTables() throws IOException {
		try {
			dbAccess.executeBatch(loadResource("create_tables.sql"));
		} catch (SQLException e) {
			throw new RuntimeException("Can not create tables:\n", e);
		}
	}

	private void dropTables() throws IOException {
		try {
			dbAccess.executeBatch(loadResource("drop_tables.sql"));
		} catch (SQLException e) {
		}
	}
	
	private List<String> loadResource(String name) throws IOException {
		InputStream inputStream = this.getClass().getResourceAsStream(name);
		BufferedReader resourceReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		List<String> result = new ArrayList<String>();
		String line;

		while ((line = resourceReader.readLine()) != null) {
			if(!line.trim().isEmpty()) {
				result.add(line);
			}
		}
		return result;
	}
}
