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
public class LoginController {
	
	
	/**
	 * Takes a url mapping and directs the user to the login html page
	 * 
	 * @return login.html
	 */
	@RequestMapping("/Login")
	public String login() {
		return "login";
	}
	
	/**
	 * Responds to a message form the login page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method checkLogin(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Login_Valid")
	@SendTo("/client/Login_ValidReturn")
	public String Login(String message) throws Exception {
		System.out.println(message);
		return checkLogin(message);
	}
	
	/**
	 * Responds to a message form the login page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method getCompletedCourses(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Login_getCompletedCourses")
	@SendTo("/client/Login_getCompletedCoursesReturn")
	public String getComp(String message) throws Exception {
		System.out.println(message);
		return getCompletedCourses(message);
	}
	
	/**
	 * Responds to a message form the login page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method getSchedules(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Login_getSchedules")
	@SendTo("/client/Login_getSchedulesReturn")
	public String getSched(String message) throws Exception {
		System.out.println(message);
		return getSchedules(message);
	}
	
	/**
	 * Uses a string sent from the client side to perorm a database query and
	 * return information as a string from the database
	 * 
	 * @param message,
	 *            the message to be used for the query
	 * @return a String o data from the data base
	 * @throws SQLException
	 */
	private String checkLogin(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String result = "";

		Scanner scan = new Scanner(message);
		String username = scan.next();
		String password = scan.next();

		rs = stmt.executeQuery("select Username, Password, Email" 
							+ " from Users u" 
							+ " where u.Username = '" + username + "' and u.Password = '" + password + "';");

		rs.next();
		if (!rs.getString("Username").equals(username) || !rs.getString("Password").equals(password)) {
			result = "fail";
		} else {
			result = "success " + rs.getString("Email");
		}

		scan.close();
		rs.close();
		stmt.close();
		conn.close();

		return result;
	}
	
	/**
	 * queries the database and returns all similar data to what the user is
	 * typing
	 * 
	 * @param message
	 *            the current text the user has typed
	 * @return result A string of data fromt eh database that is similar to what
	 *         the user typed
	 * @throws SQLException
	 */
	private String getCompletedCourses(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String result = "";

		Scanner scan = new Scanner(message);
		String username = "";
		username = scan.next();
		
		rs = stmt.executeQuery("select CourseID"
							+ " from Completed_Courses" 
							+ " where Username = '" + username + "';");

		while (rs.next()) {
			result += rs.getString("CourseID") + " ";
		}

		scan.close();
		stmt.close();
		conn.close();
		
		return result;
	}
	
	/**
	 * fetches the users schedules
	 * 
	 * @param message
	 *            the current text the user has typed
	 * @return result A string of data from the database that is similar to what
	 *         the user typed
	 * @throws SQLException
	 */
	private String getSchedules(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String result = "";

		Scanner scan = new Scanner(message);
		String username = "";
		username = scan.next();
		
		rs = stmt.executeQuery("select Schedule_name"
							+ " from Schedule" 
							+ " where Username = '" + username + "';");

		while (rs.next()) {
			result += rs.getString("Schedule_name") + ", ";
		}

		scan.close();
		stmt.close();
		conn.close();
		
		return result;
	}
}
