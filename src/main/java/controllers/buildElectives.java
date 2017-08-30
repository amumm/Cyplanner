package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import webcrawling.DBAccesses;

public class buildElectives {
	public static void main(String[] args) throws FileNotFoundException, SQLException {
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
//		File file = new File("C:/Users/mumm9/Documents/ISU/Spring2017/Coms309/Electives/SE_Tech_Electives");
//		Scanner scan = new Scanner(file);
//		scan.useDelimiter(", ");
		
//		while(scan.hasNext()){
//			DBAccesses.addToSingleColumnTable(scan.next(), "SE_Tech_Electives", conn);
//		}
		
		rs = stmt.executeQuery("select CourseID"
							+ " from Social_Sciences;");
		
		while(rs.next()){
			DBAccesses.addToSingleColumnTable(rs.getString("CourseID"), "Arts_And_Humanities_Or_Social_Sciences", conn);
		}
		
		rs = stmt.executeQuery("select CourseID"
				+ " from Arts_and_Humanities;");
		
		while(rs.next()){
			DBAccesses.addToSingleColumnTable(rs.getString("CourseID"), "Arts_And_Humanities_Or_Social_Sciences", conn);
		}
		
		stmt.close();
		conn.close();
//		scan.close();
	}
	
}
