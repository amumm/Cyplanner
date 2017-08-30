package controllers;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import webcrawling.DBAccesses;
/**
 * Write a Rating page controller and
 * connects to database and inserts values to the table and prints table
 * @author Justine Mitchell
 *
 */

@Controller
public class RatingsController{	
	/**
	 * when the /rate mapping is called it returns a html page
	 * @return writerating html page
	 */
	@RequestMapping("/rate")
	public String rate(){
		return "writerating";
	}

	/**
	 * sends message it received from the form to getQuery method
	 * @param message sent to getQuery method
	 * @return getQuery method
	 * @throws Exception getQuery method throws sql exception or error message 
	 */
	@MessageMapping("/rate")
    @SendTo("/client/rating")
    public String managing(String message) throws Exception {
		System.out.println(message);
		return getQuery(message);
	}
/**
 * Connects to database, parses string message into 4 values, inserts them into the database, and then returns the values 
 * of the table it inserted the values to
 * @param message the string message that gets parsed and the values are added to the database table
 * @return the comments table from the database to prove it wrote to it
 * @throws SQLException 
 */
	private String getQuery(String message) throws SQLException {
			String result = "<table> <tr>" + "<th>Course Id</th>" + "<th>Comment</th>" + "<th>Difficulty</th>"
					+ "<th>Dif Rating</th>" + "<th>Workload</th>"
					+ "</tr>";
			Connection conn = DBAccesses.setupDB();
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			String str=message;
			int i,j,k,m;
			String course="";
			String comment="";
			String difficulty="";
			String work="";
			for (i=0;i<str.length();i++)
			{
				if(str.charAt(i)=='-')
					break;
				else
					course=course+str.charAt(i);
			}
			for (j=i+1;j<str.length();j++)
			{
				if(str.charAt(j)=='-')
					break;
				else
					comment=comment+str.charAt(j);
			}
			for (k=j+1;k<str.length();k++)
			{
				if(str.charAt(k)=='-')
					break;
				else
					difficulty=difficulty+str.charAt(k);
			}
			for (m=k+1;m<str.length();m++)
			{
				if(str.charAt(m)=='-')
					break;
				else
					work=work+str.charAt(m);
			}
			
			/**
			 * inserts variables from parsed message into the database 
			 */
			int totalRates=0;
			double rCount=0;
			double wCount=0;
			int totalWork=0;
			double avgRate=0;
			double avgWork=0;
			stmt.executeUpdate("INSERT INTO Comments(CourseID, Comment, difficultyRating, workloadRating)"+"Values('"+course+"','"+comment+"',"+difficulty+","+work+")");
			
			rs = stmt.executeQuery("select * from Comments where CourseID='"+course+"'");
			while(rs.next())
			{
				totalRates=totalRates+rs.getInt("difficultyRating");
				totalWork=totalWork+rs.getInt("workloadRating");
				rCount=rCount+1;
				wCount=wCount+1;
			}
			if(rCount!=0&&wCount!=0)
			{
			avgRate=totalRates/rCount;
			System.out.println(avgRate);
			avgWork=totalWork/wCount;
			stmt.executeUpdate("UPDATE Course SET AvgDifRating='"+avgRate+"' WHERE CourseID='"+course+"'");
			stmt.executeUpdate("UPDATE Course SET AvgWorkloadRating='"+avgWork+"' WHERE CourseID='"+course+"'");
			}
		
			stmt.close();
			conn.close();
			rs.close();

			return result;

	}
	
}