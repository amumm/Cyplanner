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
public class Home_SingupController {

	/**
	 * Takes a url mapping and directs the user to the homePage html page
	 * 
	 * @return homePage.html
	 */
	@RequestMapping("/")
	public String profile() {
		return "home";
	}

	/**
	 * Takes a url mapping and directs the user to the signup html page
	 * 
	 * @return Signup.html
	 */
	@RequestMapping("/SignUp")
	public String signUp() {
		return "SignUp";
	}

	/**
	 * Responds to a message form the signup page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method checkSignUp(message);
	 * @throws SQLException
	 */
	@MessageMapping("/SignUp_Valid")
	@SendTo("/client/SignUp_ValidReturn")
	public String signup(String message) throws Exception {
		System.out.println(message);
		return checkSignUp(message);
	}

	/**
	 * Responds to a message form the signup page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method addCourses(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Signup_addCompletedCourses")
	@SendTo("/client/Signup_addCompletedCoursesReturn")
	public String addCourses(String message) throws Exception {
		System.out.println(message);
		return addCompletedCourses(message);
	}

	/**
	 * Responds to a message form the signup page and returns a string
	 * 
	 * @param message
	 *            This is provided from the client side
	 * @return a string that is returned from server side after being sent to
	 *         the private method checkSignUp(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Signup_AutoComp")
	@SendTo("/client/Signup_AutoCompReturn")
	public String auto(String message) throws Exception {
		System.out.println(message);
		return getSim(message);
	}

	private String checkSignUp(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String result = "";

		Scanner scan = new Scanner(message);
		String username = scan.next();
		String email = scan.next();
		String password = scan.next();

		rs = stmt.executeQuery("select Username, Password, Email" + " from Users u" + " where u.Username = '" + username
				+ "' and u.Password = '" + password + "';");

		if (rs.next())
			return "fail";
		else {
			stmt.executeUpdate(
					"Insert into Users" + " values ('" + username + "', '" + password + "', '" + email + "');");

			result = "success";
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
	private String getSim(String message) throws SQLException {

		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String result = "";
		System.out.println("before");
		Scanner scan = new Scanner(message);
		String curr = scan.next();
		System.out.println("after");
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
					"INSERT INTO Completed_Courses " + " VALUES ('" + scan.next() + "', '" + username + "');");
		}

		scan.close();
		stmt.close();
		conn.close();

		return "complete";
	}

	
}