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
/**
 * 
 * @author Andrew Mumm This class provides a mapping for the Login page of the
 *         website
 */
@Controller
public class ProfileController {
	
	/**
	 * Takes a url mapping and directs the user to the profile html page
	 * 
	 * @return profile.html
	 */
	@RequestMapping("/profile")
	public String rates() {
		return "profile";
	}
	
	/**
	 * Responds to a message form the profile page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method addCourses(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Profile_addCompletedCourses")
	@SendTo("/client/Profile_addCompletedCoursesReturn")
	public String addCoursesProf(String message) throws Exception {
		System.out.println(message);
		return addCompletedCourses(message);
	}
	@MessageMapping("/Profile_removeCompletedCourses")
	@SendTo("/client/Profile_removeCompletedCoursesReturn")
	public String removeCoursesProf(String message) throws Exception {
		System.out.println(message);
		return removeCompletedCourses(message);
	}	
	/**
	 * Responds to a message form the profile page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method addCourses(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Profile_AutoComp")
	@SendTo("/client/Profile_AutoCompleteReturn")
	public String Profile_AutoComplete(String message) throws Exception {
		System.out.println(message);
		return getSim(message);
	}
	
	/**
	 * Responds to a message form the profile page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method updateProfile(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Profile_updateFeilds")
	@SendTo("/client/Profile_updateFeildsReturn")
	public String Profile_changeFeilds(String message) throws Exception {
		System.out.println(message);
		return updateProfile(message);
	}
	/**
	 * Responds to a message form the profile page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method updateProfile(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Profile_ManCourses")
	@SendTo("/client/Profile_ManCoursesReturn")
	public String Profile_manCourses(String message) throws Exception {
		System.out.println(message);
		return updateManCourses(message);
	}
	private String updateManCourses(String message) throws SQLException
	{
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		String result=" ";
		rs = stmt.executeQuery("select * from Schedule where Schedule_name='"+message+"'");
		while (rs.next()) {
				
			result += rs.getString("Schedule_courses");
		}
		System.out.print(result);
		conn.close();
		rs.close();
		stmt.close();

		return result;
	}
	/**
	 * Responds to a message form the profile page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method updateProfile(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Profile_ManCredits")
	@SendTo("/client/Profile_ManCreditsReturn")
	public String Profile_manCredits(String message) throws Exception {
		System.out.println(message);
		return updateManCredits(message);
	}
	private String updateManCredits(String message) throws SQLException
	{
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		String result=" ";
		rs = stmt.executeQuery("select * from Schedule where Schedule_name='"+message+"'");
		while (rs.next()) {
				
			result += rs.getString("Schedule_credits");
			result+=";;;";
			result+=rs.getString("Program_name");
		}
		
		System.out.print(result);
		conn.close();
		rs.close();
		stmt.close();

		return result;
	}
	/**
	 * adds completed courses to the database
	 * 
	 * @param message
	 *            courses to be added
	 * @return result success message
	 * @throws SQLException
	 */
	private String addCompletedCourses(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();

		Scanner scan = new Scanner(message);
		String username = scan.next();

		while (scan.hasNext()) {
			stmt.executeUpdate(
					"INSERT INTO Completed_Courses " 
				 + " VALUES ('" + scan.next() + "', '" + username + "');");
		}

		scan.close();
		stmt.close();
		conn.close();

		return "complete";
	}
private String removeCompletedCourses(String message) throws SQLException
{
	Connection conn = DBAccesses.setupDB();
	Statement stmt = conn.createStatement();

	Scanner scan = new Scanner(message);
	String name = scan.next();
	System.out.print(name);

	while (scan.hasNext()) {
		String c=scan.next();
		stmt.executeUpdate(
				"DELETE FROM Completed_Courses WHERE CourseID='"+c+"'"); 
	}

	scan.close();
	stmt.close();
	conn.close();
	
	
	return "complete";
}
	/**
	 * queries the database and returns all similar data to what the user is
	 * typing
	 * 
	 * @param message
	 *            the current text the user has typed
	 * @return result A string of data from the database that is similar to what
	 *         the user typed
	 * @throws SQLException
	 */
	private String getSim(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String result = "";
		Scanner scan = new Scanner(message);
		String curr = scan.next();
		rs = stmt.executeQuery("select CourseID" + " from Course" + " where CourseID" + " like '%" + curr + "%' ;");

		while (rs.next()) {
			result += rs.getString("CourseID") + ", ";
		}

		System.out.println("result: " + result);

		scan.close();
		rs.close();
		stmt.close();
		conn.close();

		return result;
	}
	
	/**
	 * updates profile in database
	 * 
	 * @param message
	 *            data to update
	 * @return result success message
	 * @throws SQLException
	 */
	private String updateProfile(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();

		Scanner scan = new Scanner(message);
		
		System.out.println(message);
		
		String userName = scan.next();
		String newUserName = scan.next();
		String newEmail = scan.next();
		
		stmt.executeUpdate("UPDATE Users" 
						+ " SET Username = '" + newUserName + "', Email = '" + newEmail + "'"
						+ " WHERE Username = '" + userName + "';");

		stmt.executeUpdate("UPDATE Completed_Courses" 
				+ " SET Username = '" + newUserName + "'"
				+ " WHERE Username = '" + userName + "';");
		
		scan.close();
		stmt.close();
		conn.close();

		return "complete";
	}

}
