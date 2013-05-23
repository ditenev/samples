package com.sap.demo.search;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.sap.demo.search.db.DBUtil;
import com.sap.demo.search.db.DBManager;

public class SearchDemoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private DBManager dbManager;
	
	private final String[] COLUMN_NAMES = { "CLIENT", "PRODUCT_ID", "NAME", "DESCRIPTION", "RELEVANCE" };

	@Override
	public void init() throws ServletException {
		try {
			DBUtil dbUtil = new DBUtil((DataSource) new InitialContext().lookup("java:comp/env/jdbc/DefaultDB"));
			dbManager = new DBManager(dbUtil);
			dbManager.initDB();
			
		} catch (NamingException e) {
			throw new RuntimeException("Failed to lookup default datasource", e);
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to load resource", ioe);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String searchText = getSearchText(request.getParameter("searchText"));
		StringBuilder body = new StringBuilder();

		body.append("<html><body>");

		body.append(getSearchForm(searchText));
		body.append(getSearchResults(searchText));
		
		body.append("</body></html>");

		PrintWriter writer = response.getWriter();
		writer.print(body);
		writer.flush();
	}

	private String getSearchText(String searchText) {
		return searchText == null ? "mouzePat" : searchText;
	}

	private String getSearchResults(String searchText) {
		StringBuilder result = new StringBuilder();
		
		result.append("<table border='1'>");
		result.append("<tr>");
		for (String columnName : COLUMN_NAMES) {
			result.append("<th>");
			result.append(columnName);
			result.append("</th>");
		}
		result.append("</tr>");
		for (List<String> row : dbManager.getSearchResultRows(searchText, COLUMN_NAMES)) {
			result.append("<tr>");
			for (String columnValue : row) {
				result.append("<td>");
				result.append(columnValue);
				result.append("</td>");
			}
			result.append("</tr>");
		}
		result.append("</table>");
		
		return result.toString();
	}

	private String getSearchForm(String searchText) {
		StringBuilder result = new StringBuilder();

		result.append("<form name=\"input\" action=\"SearchDemoServlet\" method=\"get\">");
		result.append("Search text: <input type=\"text\" name=\"searchText\" value=\"" + searchText + "\"><br>");
		result.append("<input type=\"submit\" value=\"Submit\">");
		result.append("</form>");

		return result.toString();
	}

	
	
}
