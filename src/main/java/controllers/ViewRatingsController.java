package controllers;

import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import webcrawling.DBAccesses;
/**
 * viewRatings page controller
 * connects to the database and prints a table for the ratings based on the information entered
 * @author Justine Mitchell
 *
 */
@Controller
public class ViewRatingsController {
	/**
	 * when /viewratings page is called it returns the viewrates html page 
	 * @return viewrates html page
	 */
	@RequestMapping("/view")
	public String rates(){
		return "viewr";
	}
	/**
	 * 
	 * @param message the class that the user wants to see ratings for
	 * @return getquery method which returns a table of the ratings for that class
	 * @throws Exception sql or error
	 */
	@MessageMapping("/viewratings")
    @SendTo("/client/viewrates")
    public String managing(String message) throws Exception {
		System.out.println(message);
		return getQuery(message);
	}
	/**
	 *  connects to the database and prints a table of ratings based on the user input
	 * @param message the input value that the user gives which is the class they want to see ratings for
	 * @return a table which displays the ratings for that class
	 * @throws SQLException 
	 */
	private String getQuery(String message) throws SQLException {
		/*String result = "<table> <tr>" + "<th>Course Name</th>" + "<th>Comments</th>" + "<th>Difficulty</th>"
				 + "<th>Hours Per Week</th>"
				+ "</tr>";*/
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet r;
		ResultSet rs;
		String result="<table> <tr>"+"<th colspan = \"2\" >Course Name</th>"+"<th>Average Difficulty</th>"
		+"<th>Average Hours per Week</th>"+"</tr>";
		int avgRate;
		int avgWork;
		String CourseID = "";
		String Comments = "";
		rs=stmt.executeQuery("select * from Course where CourseID='"+message+"'");
		while(rs.next())
		{
			result += "<tr>";
			result += "<tr>";
			result += "<td colspan = \"2\">";
			CourseID = rs.getString("CourseID");
			result += CourseID;
			result += "</td>";
			result += "<td>";
			avgRate=rs.getInt("AvgDifRating");
			result+=avgRate;
			result+="</td>";
			result+="<td>";
			avgWork=rs.getInt("AvgWorkloadRating");
			result+=avgWork;
			result+="</td>";
			result+="</tr>";
		}
		rs.close();
		result += "<tr>" + "<th>Course Name</th>" + "<th>Comments</th>" + "<th>Difficulty</th>"
				 + "<th>Hours Per Week</th>"
				+ "</tr>";
		r = stmt.executeQuery("SELECT * from Comments WHERE CourseID='"+message+"'");
		int hours = 0;
		int Rating = 0;
		while (r.next()) {
			result += "<tr>";
			result += "<td>";
			CourseID = r.getString("CourseID");
			result += CourseID;
			result += "</td>";
			result += "<td>";
			Comments = r.getString("Comment");
			result += Comments;
			result += "</td>";
			result += "<td>";
			Rating = r.getInt("difficultyRating");
			result += Rating;
			result += "</td>";
			result += "<td>";
			hours= r.getInt("workloadRating");
			result += hours;
			result += "</td>";
			result += "</tr>";
		}
		result += " </table>";
		
		
		System.out.println(result);
		r.close();
		
		stmt.close();
		conn.close();
		

		return result;

}

}