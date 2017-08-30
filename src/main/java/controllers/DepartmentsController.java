package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import webcrawling.DBAccesses;



@Controller
public class DepartmentsController {
	@RequestMapping("/Departments")
	public String department(){
		return "DepartmentsDropdown";
	}
	
	@MessageMapping("/Departments-query")
    @SendTo("/client/Departments-query")
    public String managing(String message) throws Exception {
		System.out.println(message);
		
		return query(message);
	}
	
	private String query(String message) {
			String result = "<table> <tr>" + "<th>Course Id</th>" + "<th>Department</th>" + "<th>Credits</th>"
					+ "<th>Avg Dif Rating</th>" + "<th>Avg Work Load Rating</th>" + "<th>Course Description</th>"
					+ "</tr>";
			try {
				Class.forName("com.mysql.jdbc.Driver");
				System.out.println("driver reg");
			} catch (ClassNotFoundException e) {
				System.err.println("Unable to load driver.");
				e.printStackTrace();
			}

			try {
				Connection conn;
				String dbUrl = "jdbc:mysql://mysql.cs.iastate.edu:3306/db309ss1";
				String user = "dbu309ss1";
				String pass = "NzdkYjhkMTg5";
				// dbu309ss1@mysql.cs.iastate.edu:3306
				conn = DriverManager.getConnection(dbUrl, user, pass);
				System.out.println("***Connected to DB***");

				Statement stmt = conn.createStatement();
				ResultSet rs;
				rs = stmt.executeQuery("select * from Course where CourseDepartment='"+message+"';");
				System.out.println(message);
				String CourseID = "";
				String CourseDept = "";
				String Credits = "";
				int AvgDifRating = 0;
				int AvgWLRating = 0;
				String CourseDesc = "";
				while (rs.next()) {
					result += "<tr>";
					result += "<td>";
					CourseID = rs.getString("CourseID");
					result += CourseID;
					result += "</td>";
					result += "<td>";
					CourseDept = rs.getString("CourseDepartment");
					result += CourseDept;
					result += "</td>";
					result += "<td>";
					Credits = rs.getString("Credits");
					result += Credits;
					result += "</td>";
					result += "<td>";
					AvgDifRating = rs.getInt("AvgDifRating");
					result += AvgDifRating;
					result += "</td>";
					result += "<td>";
					AvgWLRating = rs.getInt("AvgWorkloadRating");
					result += AvgWLRating;
					result += "</td>";
					result += "<td>";
					CourseDesc = rs.getString("CourseDescription");
					result += CourseDesc;
					result += "</td>";

					result += "</tr>";
				}
				result += " </table>";
//				System.out.println(result);
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}

			return result;
	}
	
	@MessageMapping("/Departments-autocomplete")
    @SendTo("/client/Departments-autocomplete")
	private String getSimilar(String message) throws SQLException {

		String result = "";
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

			

		rs = stmt.executeQuery("select distinct CourseDepartment from Course where CourseDepartment"
					+ " like '%" + message + "%' ;");


		result ="";
		while(rs.next()){
			result += rs.getString("CourseDepartment") + ", ";
		}

		System.out.println("result: " + result);

		rs.close();
		stmt.close();
		conn.close();


		System.out.println("result: " + result);
		return result;
	}

}