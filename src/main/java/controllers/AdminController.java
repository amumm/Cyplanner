package controllers;

import java.sql.Connection;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import webcrawling.DBAccesses;
import webcrawling.Webcrawler;

@Controller
public class AdminController {
	/**
	 * Returns the html page to users who access the address /manage
	 * @author jacob
	 * @return
	 */
	@RequestMapping("/admin")
	public String admin(){
		return "adminPage";
	}
	
	/**
	 * handles all messages to app/admin. 
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/admin")
    @SendTo("/client/admin")
    public String admin_messages(String message) throws Exception {
		System.out.println(message);
		Connection conn = DBAccesses.setupDB();
		int[] ints=new int[2];
		String result="";
		if(message.contains("courses")){
			result="courses";
			ints=Webcrawler.populateCourses(conn);
		}else if(message.contains("prereq")){
			result="prerequisites";
			ints=Webcrawler.populatePrereqTable(conn);
		}else if(message.contains("geneds")){
			result="Gen Ed courses";
			ints=Webcrawler.populateGenEdTables(conn);
		}else if(message.contains("USdiv")){
			result="US Diversity courses";
			ints=Webcrawler.populateUSDiversityTable(conn);
		}else if(message.contains("International")){
			result="International Perspective courses";
			ints=Webcrawler.populateInternationalPerspectiveTable(conn);
		}
		message="Found "+ints[0]+" "+result+"; added "+ints[1]+" of them.";
		conn.close();
		return message;
	}

	
}
