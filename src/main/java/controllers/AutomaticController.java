package controllers;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import webcrawling.DBAccesses;

/**
 *
 * @author Andrew Mumm
 *This class provides a mapping for the auto schedule page of the website
 */
@Controller
public class AutomaticController {

	/**
	 * Takes a url mapping and directs
	 * the user to the automatic html page
	 *
	 * @return automatic.html
	 */
	@RequestMapping("/Auto")
	public String auto(){
		return "automatic";
	}

	/**
	 * Responds to a message from the auto schedule page
	 * and returns a string to be added to a text box
	 * @param message
	 * This is provided from the client side
	 * @return a string that is returned from server
	 *  side after being sent to the private method query(message);
	 * @throws SQLException
	 */
	@MessageMapping("/AutoAdd")
    @SendTo("/client/add")
    public String add(String message) throws Exception {
		System.out.println(message);
		return query(message);
	}

	/**
	 * Responds to a message form the auto schedule page
	 * and returns a string of what is to be removed from
	 * a textbox
	 * @param message
	 * This is provided from the client side
	 * @return a string that is returned from server
	 *  side after being sent to the private method removeLast(message);
	 * @throws SQLException
	 */
	@MessageMapping("/AutoRemove")
    @SendTo("/client/remove")
    public String remove(String message) throws Exception {
		System.out.println(message);
		return removeLast(message);
	}

	/**
	 * Responds to a message form the auto schedule page
	 * and returns a string o data to be used or auto-complete
	 * functionality
	 * @param message
	 * This is provided from the client side
	 * @return a string that is returned from server
	 *  side after being sent to the private method getSimilar(message);
	 * @throws SQLException
	 */
	@MessageMapping("/AutoComplete")
    @SendTo("/client/complete")
    public String complete(String message) throws Exception {
		System.out.println(message);
		return getSimilar(message);
	}

	/**
	 * Responds to a message form the auto schedule page
	 * and returns a string o data to be used or auto-complete
	 * functionality
	 * @param message
	 * This is provided from the client side
	 * @return a string that is returned from server
	 *  side after being sent to the private method getSimilar(message);
	 * @throws SQLException
	 */
	@MessageMapping("/Generate")
   @SendTo("/client/generate")
   public String generate(String message) throws Exception {
		System.out.println(message);
		return topoSort(message).toJSONString();
	}

	/**
	 * This method performs a topological sort on the courses that
	 * a student will be taking and helps to generate a schedule
	 * in a format that can be used
	 * @return result
	 * an array list of all the courses a student will take in
	 * sorted order
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	private JSONArray topoSort(String info) throws SQLException, FileNotFoundException{
		ArrayList<CourseSort> courseList = new ArrayList<CourseSort>();
		ArrayList<CourseSort> queue = new ArrayList<CourseSort>();
		ArrayList<CourseSort> result = new ArrayList<CourseSort>();
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		ArrayList<Semester> semesters = new ArrayList<Semester>();
		ArrayList<CourseSort> requirements = new ArrayList<CourseSort>();
		
		Scanner scan = new Scanner(info);
		scan.useDelimiter(", ");
		String user = scan.next();
		
		int numProgs = 0;
		String programs = "";
		while(scan.hasNext()){
			numProgs++;
			programs += scan.next() + ", ";
		}
		scan.close();

		int numSems = numProgs * 8;
		int currSemNum = 0;
		for(int i = 0; i < numSems; i++){
			semesters.add(new Semester(i + 1));
		}

		//  1) Get all the courses that will need to be taken and add all of those courses to courseList
		//  2) loop through courseList and add all of the preReqs that the course has	
		//  3) Get requirements
		//write to test file
		courseList = topoCourseSort.getCourses(programs);
		topoCourseSort.addCourseListPreReqs(courseList);
//		System.out.println(courseList.size());
		topoCourseSort.removeCompletedCourses(courseList, user);
//		System.out.println(courseList.size());
		requirements = topoCourseSort.getReqs(programs);
		
		int totalCredits = topoCourseSort.getTotalCredits(courseList, requirements);
//		double creditsPerSem = ((double)totalCredits)/((double)numSems);
		double creditsPerSem = 12;
//		topoCourseSort.testBeforeSort(courseList);
		topoCourseSort.sprinkleReqs(requirements, semesters);
		
//		System.out.println(totalCredits);
		
		int failNum = 0;
		Semester currSem = semesters.get(currSemNum);
		CourseSort temp;
		int listSizeLast = courseList.size();
		int listSizeCurr = courseList.size();
		while(courseList.size() != 0 || queue.size() != 0 || requirements.size() != 0){
			
			//  3) If a course has 0 preReqs, add it to the queue and remove from courseList
			topoCourseSort.addToQueue(courseList, queue, toRemove, currSem);
			listSizeCurr = courseList.size();

			//  4) add the first element of queue to result and remove from queue
			if(queue.size() == 0 && courseList.size() == 0 && requirements.size() != 0){
				for(int i = 0; i < requirements.size(); i++){
					result.add(requirements.get(0));
					currSem.addCourse(requirements.get(0));
					// increment the semester if neccessary
					if(currSem.getCredits() >= creditsPerSem){
						currSemNum++;
						if(currSemNum == numSems){
							numSems++;
							semesters.add(new Semester(currSemNum + 1));
							currSem = semesters.get(currSemNum);
						}
						else
							currSem = semesters.get(currSemNum);
					}
					requirements.remove(0);
				}
			}
			else if(queue.size() == 0 && requirements.size() != 0){
				result.add(requirements.get(0));
				currSem.addCourse(requirements.get(0));
				requirements.remove(0);
			}
			else{
				if(queue.size() != 0){
					result.add(queue.get(0));
					currSem.addCourse(queue.get(0));
					temp = queue.get(0);
					queue.remove(0);
					
					//  5) remove the element added to result from all other courses' preReqs List
					topoCourseSort.removePreReqs(courseList, temp, toRemove);
				}
				
				
			}
			
			// increment the semester if neccessary
			if(currSem.getCredits() >= creditsPerSem){
				currSemNum++;
				if(currSemNum == numSems){
					numSems++;
					semesters.add(new Semester(currSemNum + 1));
					currSem = semesters.get(currSemNum);
				}
				else
					currSem = semesters.get(currSemNum);
			}
			

			//  6) repeat the process until the queue is empty
			//  7) if the queue is empty and courseList is not then there is an error
			if(queue.size() == 0 && courseList.size() != 0 && requirements.size() == 0){
				if(listSizeLast != listSizeCurr){
					listSizeLast = listSizeCurr;
					
				}
				else{
					System.out.println("courseList: " + courseList.size());
					System.out.println("Queue: " + queue.size());
					System.out.println("requirements: " + requirements.size());
					System.out.println("Error in topo sort");
					break;
				}
			}
		}

		System.out.println("Finished sorting");

		//write to test file
//		topoCourseSort.testAfterSort(result, programs);
//		topoCourseSort.testSemesters(semesters);
//		topoCourseSort.buildTable(result, semesters);
		
		JSONArray sems = new JSONArray();
		JSONArray sem;
		JSONObject semCourse;
		Semester semTemp;
		
		for(int i = 0; i < semesters.size(); i++){
			sem = new JSONArray();
			sems.add(sem);
			semTemp = semesters.get(i);
			for(int k = 0; k < semesters.get(i).courses.size(); k++){
				semCourse = new JSONObject();
				semCourse.put("CourseID", semTemp.courses.get(k).getName());
				semCourse.put("Credits", semTemp.courses.get(k).getCredits());
				((JSONArray)sems.get(i)).add(semCourse);
			}
		}
			
	     
//	    System.out.println(sems);
		return sems;
	}

	/**
	 * queries the database and returns all similar data to
	 *  what the user is typing
	 * @param message
	 * the current text the user has typed
	 * @return result
	 * A string of data fromt eh database that is similar to what
	 * the user typed
	 * @throws SQLException
	 */
	private String getSimilar(String message) throws SQLException {

		String result = "";
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

			Scanner scan = new Scanner(message);
			String majOrMin = scan.next();
			String choice = scan.next();
			String type;

			while(scan.hasNext()){
				choice += " " + scan.next();
			}

			System.out.println(choice);

			if(majOrMin.equals("Majors")){
				type = "major";
			}
			else{
				type = "minor";
			}

			rs = stmt.executeQuery("select Program_name"
					+ " from Programs"
					+ " where Program_name"
					+ " like '%" + choice + "%' ;");


			result = majOrMin + ", ";
			while(rs.next()){
				result += rs.getString("Program_name") + ", ";
			}

			System.out.println("result: " + result);

			scan.close();
			rs.close();
			stmt.close();
			conn.close();


		System.out.println("result: " + result);
		return result;
	}

	/**
	 * Removes the last majr or minor that was inout by the user
	 * @param curr
	 * the current string the is in the output
	 * @return result
	 * a string with the last major or minor in curr removed
	 */
	private String removeLast(String curr){
		String result = "";
		Scanner scan = new Scanner(curr);
		result = scan.next();
		String temp = "";
		scan.useDelimiter(", ");

		int numEntries = 0;
		while(scan.hasNext()){
			temp += scan.next() + ", ";
			numEntries++;
		}
		int totalEntries = numEntries;
		Scanner scanTemp = new Scanner(temp);
		scanTemp.useDelimiter(",");


		while(numEntries > 2){
			result += scanTemp.next() + ",";
			numEntries--;
		}
		if(totalEntries <= 1) return result + " ";
		result += scanTemp.next();


		scan.close();
		return result;
	}

	/**
	 * queries the database to check whether or not a major or minor exists
	 * @param message
	 * the major or minor being checked
	 * @return result
	 * null if the major or minor does not exist and if it does it returns the program
	 * @throws SQLException
	 */
	private String query(String message) throws SQLException {

			String result = "";
			Connection conn = DBAccesses.setupDB();
			Statement stmt = conn.createStatement();
			ResultSet rs;

				Scanner scan = new Scanner(message);
				String majOrMin = scan.next();
				String choice = scan.next();
				String type;

				while(scan.hasNext()){
					choice += " " + scan.next();
				}

				System.out.println(choice);

				if(majOrMin.equals("Majors")){
					type = "major";
				}
				else{
					type = "minor";
				}

				rs = stmt.executeQuery("select Program_name"
						+ " from Programs"
						+ " where Program_name = '" + choice + "';");
				rs.next();

				result = majOrMin + " " + rs.getString("Program_name");

				scan.close();
				rs.close();
				stmt.close();
				conn.close();

			return result;
	}
}
