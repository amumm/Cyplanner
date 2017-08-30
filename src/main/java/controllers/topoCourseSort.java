package controllers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import webcrawling.DBAccesses;
import webcrawling.DBAccesses.Course;

import java.util.regex.Matcher;

public class topoCourseSort {
	
	

	public static ArrayList<CourseSort> getCourses(String programs) throws SQLException{
		ArrayList<CourseSort> courseList = new ArrayList<CourseSort>();
		
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn.createStatement();
		ResultSet rs, rs2;
		
		String tempProgram;
		String CourseID;
		
		Scanner scan = new Scanner(programs);
		scan.useDelimiter(", ");
		while(scan.hasNext()){
			tempProgram = scan.next();
			rs = stmt.executeQuery("select CourseID"
								+ " from " + tempProgram + "_major;");
			while(rs.next()){
				CourseID = rs.getString("CourseID");
				rs2 = stmt2.executeQuery("select Credits"
									 + " from Course"
									 + " where CourseID = '" + CourseID + "';");
				rs2.next();
				courseList.add(new CourseSort(CourseID, tempProgram, rs2.getString("Credits")));
			}
		}
		
		
		scan.close();
		stmt.close();
		conn.close();
		
		return courseList;
	}
	
	public static ArrayList<CourseSort> getReqs(String programs) throws SQLException{
		ArrayList<CourseSort> Reqs = new ArrayList<CourseSort>();
		
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		String tempProgram;
		String CourseID;
		
		int numCourses;
		
		
		Scanner scan = new Scanner(programs);
		scan.useDelimiter(", ");
		while(scan.hasNext()){
			tempProgram = scan.next();
			rs = stmt.executeQuery("select Requirement_name, Credits_required"
								+ " from Requirements"
								+ " where Program_name = '" + tempProgram + "';");
			while(rs.next()){
				numCourses = Integer.parseInt(rs.getString("Credits_required")) / 3;
				CourseID = rs.getString("Requirement_name");
				for(int i = 0; i < numCourses; i++){
					Reqs.add(new CourseSort(CourseID, tempProgram, "3"));
				}
			}
		}
		
		
		scan.close();
		stmt.close();
		conn.close();
		
		return Reqs;
	}
	
	public static void sprinkleReqs(ArrayList<CourseSort> Reqs, ArrayList<Semester> semesters) {
		ArrayList<CourseSort> techLike = new ArrayList<CourseSort>();
		ArrayList<CourseSort> nonTech = new ArrayList<CourseSort>();
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		
		for(int i = 0; i < Reqs.size(); i++){
			if(Reqs.get(i).getName().contains("SE_Ele") || 
			   Reqs.get(i).getName().contains("Supp") ||
			   Reqs.get(i).getName().contains("Tech")){
				techLike.add(Reqs.get(i));
			}
			else{
				nonTech.add(Reqs.get(i));
				toRemove.add(i);
				
			}
			
		}
		for(int k = toRemove.size() - 1; k > -1; k--){
			Reqs.remove((int)toRemove.get(k));
		}
	
		if(nonTech.size() != 0){
			semesters.get(0).addCourse(nonTech.get(0));
			nonTech.remove(0);
		}
		int semNum = 1;
		while(nonTech.size() != 0){
			semesters.get(semNum).addCourse(nonTech.get(0));
			nonTech.remove(0);
			if(nonTech.size() != 0){
				semesters.get(semNum).addCourse(nonTech.get(0));
				nonTech.remove(0);
			}
			semNum++;
		}
		
	}
	
	public static int getTotalCredits(ArrayList<CourseSort> CourseList, ArrayList<CourseSort> Reqs){
		
		int total = 0;
		final Pattern patternAllNums = Pattern.compile("^[0-9]+$");
		final Pattern containsNums = Pattern.compile(".*[0-9].*");
		
		for(int i = 0; i < CourseList.size(); i++){
			if(containsNums.matcher(CourseList.get(i).getCredits()).matches()){
				if(patternAllNums.matcher(CourseList.get(i).getCredits()).matches())
					total += Integer.parseInt(CourseList.get(i).getCredits());
				else
					total += (int)(CourseList.get(i).getCredits().charAt(0));
			}
		}
		
		for(int k = 0; k < Reqs.size(); k++){
			if(containsNums.matcher(Reqs.get(k).getCredits()).matches()){
				if(patternAllNums.matcher(Reqs.get(k).getCredits()).matches())
					total += Integer.parseInt(Reqs.get(k).getCredits());
				else
					total += (int)(Reqs.get(k).getCredits().charAt(0));
			}
		}
		
		
		return total;
	}
	
	public static void addCourseListPreReqs(ArrayList<CourseSort> courseList) throws SQLException{
		
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn.createStatement();
		ResultSet rs, rs2;
		
		CourseSort tempCourse;
		Boolean need = true;
		for(int i = 0;  i < courseList.size(); i++){
			rs = stmt.executeQuery("select preReqID"
						+ " from Prerequisites"
						+ " where CourseID = '" + courseList.get(i).getName() + "';");

			while(rs.next()){
				rs2 = stmt2.executeQuery("select Credits"
						 + " from Course"
						 + " where CourseID = '" + rs.getString("preReqID") + "';");
				rs2.next();
				tempCourse = new CourseSort(rs.getString("preReqID"), courseList.get(i).getProgram(), rs2.getString("Credits"));
				courseList.get(i).addPreReq(tempCourse);
				
				for(int j = 0; j < courseList.size(); j++){
					if(tempCourse.getName().equals(courseList.get(j).getName())){
						need = false;
						break;
					}
				}
				if(need){
					DBAccesses.addToSingleColumnTable(tempCourse.getName(), (tempCourse.getProgram() + "_major"), conn);
					courseList.add(tempCourse);
				}
				need = true;
			}
		}
		
		stmt.close();
		conn.close();
	}
	
public static void removeCompletedCourses(ArrayList<CourseSort> courseList, String user) throws SQLException{
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn.createStatement();
		ResultSet rs, rs2;
		
		ArrayList<String> completedCourses = new ArrayList<String>();
		
		rs = stmt.executeQuery("select CourseID"
					+ " from Completed_Courses"
					+ " where Username = '" + user + "';");
		while(rs.next()){
			completedCourses.add(rs.getString("CourseID"));
		}
		int i = 0;
		while(i < completedCourses.size()){
			rs2 = stmt2.executeQuery("select preReqID"
					+ " from Prerequisites"
					+ " where CourseID = '" + completedCourses.get(i) + "';");
			while(rs2.next()){
				completedCourses.add(rs2.getString("preReqID"));
			}
			i++;
		}
			
		while(completedCourses.size() != 0){
			for(int k = courseList.size() - 1; k > - 1; k--){
				if(completedCourses.get(0).equals(courseList.get(k).getName())){
					courseList.remove(k);
				}
				else{
					for(int j = courseList.get(k).getPreReqs().size() - 1; j > - 1; j--){
						if(completedCourses.get(0).equals(courseList.get(k).getPreReqs().get(j).getName())){
							courseList.get(k).removePreReq(courseList.get(k).getPreReqs().get(j));
						}
					}
				}
			}
			completedCourses.remove(0);
		}
		
		
		stmt.close();
		stmt2.close();
		conn.close();
	}
	
	
	public static void addToQueue(ArrayList<CourseSort> courseList, ArrayList<CourseSort> queue, ArrayList<Integer> toRemove, Semester sem) throws SQLException{
		
		for(int i = 0;  i < courseList.size(); i++){
			if(courseList.get(i).getNumPreReqs() == 0){
				if(canAdd(sem, courseList.get(i))){
					queue.add(courseList.get(i));
					toRemove.add(i);
				}
			}
		}

		for(int i = toRemove.size() - 1; i > -1;i--){
			courseList.remove(courseList.get(toRemove.get(i)));
		}
		toRemove.clear();
		
	}
	
	public static void removePreReqs(ArrayList<CourseSort> courseList, CourseSort temp, ArrayList<Integer> toRemove) throws SQLException{
		
		for(int i = 0;  i < courseList.size(); i++){
			for (int j = 0; j < courseList.get(i).getPreReqs().size(); j++){
				if(courseList.get(i).getPreReqs().get(j).getName().equals(temp.getName())){
					toRemove.add(j);
				}
			}
			for(int k = toRemove.size() - 1; k > -1;k--){
				//add to compPreReqs
				courseList.get(i).getCompPreReqs().add(courseList.get(i).getPreReqs().get(toRemove.get(k))); 
				//remove from preReqs
				courseList.get(i).removePreReq(courseList.get(i).getPreReqs().get(toRemove.get(k)));
			}
			toRemove.clear();
		}
		
	}
	
	public static void testBeforeSort(ArrayList<CourseSort> courseList) throws SQLException, FileNotFoundException{
		
		PrintWriter test = new PrintWriter("src/test/auto_output/courseList");
		for (int i = 0; i < courseList.size(); i++) {
			test.println((i + 1) + ". " + courseList.get(i).getName() + ", " + courseList.get(i).getProgram() + ":");
			for(int k = 0; k < courseList.get(i).getPreReqs().size(); k++){
				test.println("\t" + (k + 1) + ". " + courseList.get(i).getPreReqs().get(k).getName() + ", "
							+ courseList.get(i).getProgram() + ":");
			}
		}
		
		test.close();
	}
	
	public static void testAfterSort(ArrayList<CourseSort> result, String programs) throws SQLException, FileNotFoundException{
		
		Scanner scan = new Scanner(programs);
		PrintWriter p, a;
		String tempProgram;
		
		while(scan.hasNext()){
			tempProgram = scan.next();
			p = new PrintWriter("src/test/auto_output/" + tempProgram + "_shedule");
			for (int i = 0; i < result.size(); i++) {
				if(tempProgram.equals(result.get(i).getProgram()))
					p.println(result.get(i).getName());
			}
			p.close();
		}
		a = new PrintWriter("src/test/auto_output/full_shedule");
		for (int i = 0; i < result.size(); i++) {
			a.println(result.get(i).getName());
		}
		a.close();
		
		scan.close();
	}
	
	public static void testSemesters(ArrayList<Semester> semesters) throws FileNotFoundException{
		
		PrintWriter test = new PrintWriter("src/test/auto_output/semesters");
		Semester tempSem;
		for(int i = 0; i < semesters.size(); i++){
			tempSem = semesters.get(i);
			for(int k = 0; k < tempSem.courses.size(); k++){
				test.println((i + 1) + ". " + tempSem.courses.get(k).getName() +  ":");
			}
			test.println("");
		}
		test.close();
	}
	
	public static String buildTable(ArrayList<CourseSort> result, ArrayList<Semester> semesters) throws SQLException, FileNotFoundException{
		
		String table = "";
		Semester temp;
		for(int i = 0; i < semesters.size(); i++){
			temp = semesters.get(i);
			table += "<table style=\"width:25%\">";
			table += "<tr><th style = \"text-align: left\" colspan=\"2\">Semester: " + temp.getSemNum() + "</th></tr>";
			table += "<tr><th style = \"text-align: center\" >Course</th><th style = \"text-align: center\" >Credits</th></tr>";
			
			for(int k = 0; k < temp.courses.size(); k++){
				table += "<tr><td align = \"center\" >" + temp.courses.get(k).getName().trim() + "</td>"
						+ "<td align = \"center\" >" + temp.courses.get(k).getCredits().trim() + "</td></tr>";
			}
			table += "<tr><th align = \"center\" > Total Credits </th><th align = \"center\" >" 
				  + temp.getCredits() + "</th></tr>";
			table += "</table>";
		}
		
		return table;
	}
	
	private static boolean canAdd(Semester sem, CourseSort course){
		
		if(sem.courses.size() == 0) return true;
		
		for(int i = 0; i < course.compPreReqs.size(); i++) {
			for(int k = 0; k < sem.courses.size(); k++){
				if(course.compPreReqs.get(i).getName().equals(sem.courses.get(k).getName()))
					return false;
			}
		}
			
		return true;
	}
	
}
