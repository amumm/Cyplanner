package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import webcrawling.DBAccesses;



@Controller
public class ManageController {
	
	/**
	 * Returns the html page to users who access the address /manage
	 * @author jacob
	 * @return
	 */
	@RequestMapping("/manage")
	public String manage(){
		return "Manage_Page";
	}
	
	@MessageMapping("/manageSchedule")
    @SendTo("/client/manage-Schedule")
    public String manageSchedule(String message) throws Exception {
		System.out.println("message:"+message);
		
		return addSchedule(message);
	}
	private String addSchedule(String message)throws SQLException
	{
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		Scanner scan=new Scanner(message);
		
		String username = scan.next();
		
		stmt.executeUpdate("INSERT INTO Schedule(Username, Schedule_name, Schedule_courses, Schedule_credits, Program_name)"+"Values('"+username+"','"+scan.next()+"','"+scan.next()+"','"+scan.next()+"','"+scan.next()+"')");
		
		Statement stmt2 = conn.createStatement();
		ResultSet rs2;

		String result = "";
		
		rs2 = stmt2.executeQuery("select Schedule_name"
							+ " from Schedule" 
							+ " where Username = '" + username + "';");
		
		while (rs2.next()) {
			result += rs2.getString("Schedule_name") + ", ";
		}
		
		scan.close();
		conn.close();
		stmt.close();
		stmt2.close();
		
		return result;
		
	}
	/**
	 * handles all messages to app/manage-requirements. Returns all the requirements of the 
	 * major who's name was sent to the server
	 * 
	 * @param message - the name of the major
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/manage-requirements")
    @SendTo("/client/manage-requirements")
    public String managing(String message) throws Exception {
		System.out.println("message:"+message);
		
		return get_requirements(message);
	}
	
	@MessageMapping("/manage-validation")
    @SendTo("/client/manage-validation")
    public String managingValidation(String message) throws Exception {
		System.out.println(message);
		JSONParser parser = new JSONParser();
		JSONObject to_validate = (JSONObject)parser.parse(message.split(" ;;; ")[0]);
		JSONArray courses=(JSONArray)to_validate.get("courses");
		JSONArray credits=(JSONArray)to_validate.get("credits");

		for (int i =0; i<courses.size(); i++) {
			for (int j =0; j< ((JSONArray)courses.get(i)).size(); j++) {
				System.out.println(((JSONArray)courses.get(i)).get(j));
				System.out.println(((JSONArray)credits.get(i)).get(j));
			}
		}
		
		Connection conn = DBAccesses.setupDB();
		Statement stmt_requirements;
		ResultSet rs_requirements;
		Statement stmt_major;
		ResultSet rs_major;
		
		Statement stmt_temp;
		ResultSet rs_temp;
		//get requirements for message.split[1] from database
		stmt_requirements = conn.createStatement();
		rs_requirements = stmt_requirements.executeQuery("select Requirement_name,Credits_required from Requirements where Program_Name ="
				+ "'" + message.split(" ;;; ")[1] + "';");
		ArrayList<Integer> req_creds=new ArrayList<Integer>();//# of credits you need for each requirement
		ArrayList<String> req_names=new ArrayList<String>();
		ArrayList<ArrayList<String>> req_rs=new ArrayList<ArrayList<String>>();//all the requirements courses
//		final Pattern patternAllNums = Pattern.compile("^[0-9]+$");
		
		//get all the requirement lists from the database and save them
		while(rs_requirements.next()){
			req_creds.add(rs_requirements.getInt("Credits_required"));
			req_names.add(rs_requirements.getString("Requirement_name"));
			stmt_temp=conn.createStatement();
			if(!rs_requirements.getString("Requirement_name").equals("Open_Elective"))
				rs_temp=stmt_temp.executeQuery("select CourseID,Credits from Course where CourseID in (select CourseID from "+rs_requirements.getString("Requirement_name")+")");
			else
				rs_temp=null;
			
			if(rs_temp==null){
				req_rs.add(null);
				
			}
			else{
				req_rs.add(new ArrayList<String>());
			}
			while(rs_temp!=null&&rs_temp.next()){
				try{
					req_rs.get(req_rs.size()-1).add(rs_temp.getString("CourseID"));
				}catch(Exception e){
					System.out.println(e.getMessage());
					req_rs.get(req_rs.size()-1).add(null);
				}
			}
			
		}
		
		
		//go through _major and make sure all of those classes are there
		stmt_major=conn.createStatement();
		rs_major=stmt_major.executeQuery("select CourseID from "+message.split(" ;;; ")[1]+"_major");
		ArrayList<String> result_arr=new ArrayList<String>();
		ArrayList<String> result_reasons=new ArrayList<String>();
		while(rs_major.next()){
			for (int i =0; i<courses.size(); i++) {
				if(((JSONArray)courses.get(i)).contains(rs_major.getString("CourseID"))){
					break;
				}else if(i==courses.size()-1){
					result_arr.add(rs_major.getString("CourseID"));
					result_reasons.add("Required Class");
				}
			}
		}
		
		int num_courses_used=0;
		int num_courses=0;
		//go through each requirement, and if you find a class that's in there, 
		//  subtract that course's # of credits from the current requirement you're
		//  lookin at
		for (int i =0; i<courses.size(); i++) {
			num_courses+=((JSONArray)courses.get(i)).size();
			for (int j =0; j< ((JSONArray)courses.get(i)).size(); j++) {
				if(((String)((JSONArray)courses.get(i)).get(j)).contains("_Elective") ||
						((String)((JSONArray)courses.get(i)).get(j)).contains("_Humanities") ||
						((String)((JSONArray)courses.get(i)).get(j)).contains("Social_")){
					for(int k=0;k<req_names.size();k++){
							if(req_names.get(k).equals(((JSONArray)courses.get(i)).get(j))){
								if(req_creds.get(k)>0 )
									num_courses_used++;
								req_creds.set(k, (int)(req_creds.get(k)-3));
							}
					}		
					continue;
				}
				for(int k=0;k<req_rs.size();k++){
					if(req_rs.get(k)!=null){
						if(req_rs.get(k).contains(((JSONArray)courses.get(i)).get(j))){
							if(req_creds.get(k)>0)
								num_courses_used++;
							req_creds.set(k, (int)(req_creds.get(k)-(long)((JSONArray)credits.get(i)).get(j)));
							
						}
					}else{
						//open_elective
					
					}
				}
			}
		}
		//every requirement/class you find that's not met, add it to the return string
		for(int i=0;i<req_creds.size();i++){
			if(req_names.get(i).equals("Open_Elective")){
				//if there is an extra course, it's used for open elec; 
				//if there are less courses used then total courses, open elec is good; thus we
				//not < here so that if there aren't any extra courses, open_elective is flagged as not met
				if(num_courses_used>=num_courses){
//					System.out.println("found open elec");
					System.out.println("num courses used:"+num_courses_used+"size: "+num_courses);
					result_arr.add(req_names.get(i));
					result_reasons.add("Requirement Not Met");
				}
			}
			else if(req_creds.get(i)>0){
				result_arr.add(req_names.get(i));
				result_reasons.add("Requirement Not Met");
			}
		}
		
		for(int i=0;i<req_creds.size();i++){
			System.out.println(req_names.get(i)+": "+req_creds.get(i));
		}
		
		
//		System.out.println(to_validate.toJSONString());
		
		/* 
		 * 					Order Validation
		 * ---------------------------------------------------- *
		 * For each course in the array recieved from the client
		 *  pull it's prereqs
		 *  for each prereq
		 *   if that prereq is not in any of the previous semesters<-- saved in a hashset (and it's equivalents aren't)
		 *    then add that course to the result array, with the reason 
		 *    "prerequisites not met"
		 * 
		 */
		
		Statement stmt_ordering=conn.createStatement();
		ResultSet rs_ordering;
		HashSet<String> previously_taken_courses=new HashSet<String>();
		String equiv_array[];
		boolean equiv_met=false;
		//skip semester 0
		for(int i=1;i<courses.size();i++){//for each semester
			for(int k=0;k<i;k++){
				for(int j=0; j<((JSONArray)courses.get(k)).size(); j++){
					previously_taken_courses.add((String)((JSONArray)courses.get(k)).get(j));
				}
			}
			
			for(int j=0; j< ((JSONArray)courses.get(i)).size(); j++ ){//for each course in each semester
				rs_ordering=stmt_ordering.executeQuery("select PrereqID,PrereqEquivalents from Prerequisites where CourseID='"+((JSONArray)courses.get(i)).get(j)+"'");
				
				while(rs_ordering.next()){
					if(rs_ordering.getString("PrereqEquivalents")!=null && !rs_ordering.getString("PrereqEquivalents").equals("")){
						equiv_array=rs_ordering.getString("PrereqEquivalents").split(" ");
						for(int l=0;l<equiv_array.length;l++){
							if(!equiv_array[l].equals("") && previously_taken_courses.contains(equiv_array[l])){
								equiv_met=true;
								break;
							}
						}
						if(equiv_met) {
							equiv_met=false;
							continue;
						}
					}
					if(!previously_taken_courses.contains(rs_ordering.getString("PrereqID"))){
						System.out.print("prereq:"+rs_ordering.getString("PrereqID")+"  ");
						result_arr.add((String)((JSONArray)courses.get(i)).get(j));
						result_reasons.add("Prereq not met:"+rs_ordering.getString("PrereqID"));
					}
				}
			}
		}
		
		
		//convert back to JSON and send it to client
		JSONArray endobj=new JSONArray();
		endobj.add(result_arr);
		endobj.add(result_reasons);
		return endobj.toJSONString();
	}

	private String get_requirements(String message) {
		String result = "";
		Connection conn = DBAccesses.setupDB();
		Statement stmt_requirements;
		ResultSet rs;
		
		try {
			stmt_requirements = conn.createStatement();
			rs = stmt_requirements.executeQuery("select Requirement_name,Credits_required from Requirements where Program_Name ="
					+ "'" + message + "';");
			result+=create_requirement_div(message+"_major", "", conn);
			while(rs.next()){
				if(rs.getString("Requirement_name").equals("Open_Elective")){
					result+="<div class='requirement_div' id='Open_Elective'><table style='width:100%'><tr><th style='cursor:pointer;'>Open_Elective (any class): "+rs.getString("Credits_required")+" credits </th></tr></table></div>";
				}else{
					System.out.println(rs.getString("Requirement_name"));
					result+=create_requirement_div(rs.getString("Requirement_name"),rs.getString("Credits_required"),conn);
				}
			}
			return result;			
		} catch (SQLException e) {
			e.printStackTrace();
			return "failed to find that major";
		}
		
		

		
	}
	private String create_requirement_div(String table_name, String credits,Connection conn){
		String result="<div class='requirement_div' id='"+table_name+"_div'>";
		ResultSet rs;
		try {
			Statement stmt=conn.createStatement();
			rs=stmt.executeQuery("select CourseID,Credits,CourseTitle from Course where CourseID in (select CourseID from "+table_name+")");
			result+="<table style='width:100%'>";
			result+="<tr>";
			result+="<th class='requirement_header' id='"+table_name+"' colspan='3' style='cursor:pointer;'>";
			result+=table_name;
			if(!credits.equals(""))
				result+=": "+credits+" credits";
			result+="</th>";
			result+="</tr>";
			result+="<tr class='"+table_name+"_row' style='display:none; width: 100%;'>";
			result+="<th>Course ID</th><th>Title</th><th>Credits</th>";
			result+="</tr>";
			while(rs.next()){
				result+="<tr class='"+table_name+"_row requirement_row' id='"+rs.getString("CourseID")+"' style='cursor:pointer; display:none; width: 100%;'>";
				result+="<td>";
				result+=rs.getString("CourseID");
				result+="</td>";
				result+="<td>";
				result+=rs.getString("CourseTitle");
				result+="</td>";
				result+="<td>";
				result+=rs.getString("Credits");
				result+="</td>";
				result+="</tr>";
			}
			result+="</table>";
		} catch (Exception e) {
			System.out.println("failed querying of "+table_name);
			e.printStackTrace();
		}
		
		result+="</div>";
		return result;
	}
	
	@MessageMapping("/manage-autocomplete")
    @SendTo("/client/manage-autocomplete")
	private String program_name_auto(String message) throws SQLException {

		String result = "";
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

			

		rs = stmt.executeQuery("select distinct Program_name from Requirements where Program_name"
					+ " like '%" + message + "%' ;");


		result ="";
		while(rs.next()){
			result += rs.getString("Program_name") + ", ";
		}

		System.out.println("result: " + result);

		rs.close();
		stmt.close();
		conn.close();


		System.out.println("result: " + result);
		return result;
	}
	
	@MessageMapping("/manage-autocomplete-course")
    @SendTo("/client/manage-autocomplete-course")
	private String course_auto(String message) throws SQLException {

		String result = "";
		Connection conn = DBAccesses.setupDB();
		Statement stmt = conn.createStatement();
		ResultSet rs;

			

		rs = stmt.executeQuery("select CourseID from Course where CourseID"
					+ " like '%" + message + "%' ;");


		result ="";
		while(rs.next()){
			result += rs.getString("CourseID") + ", ";
		}

//		System.out.println("result: " + result);

		rs.close();
		stmt.close();
		conn.close();


//		System.out.println("result: " + result);
		return result;
	}
	
	@MessageMapping("/manage-semesters")
    @SendTo("/client/manage-semesters")
    public String managing_semesters(String message) throws Exception {
		System.out.println("message:"+message);
		
		return get_semesters(message);
	}

	private String get_semesters(String message) {
		String result = "";
		if(message.equals("create")){
			for(int i =-1;i<8;i++){
				result +="<div class='semester_table'id='semester_"+(i+1)+"'>";
				result +="<table style='width: 100%;'>";
				result +="<tr style='width: 100%;'>";
				result +="<th colspan='2'>Semester "+(i+1);
				if(i+1==0) result+=" (Classes taken before attending ISU)";
				result +="</th>";
				result +="</tr>";
				result +="<tr style='width: 100%;'>";
				result +="<th>Course ID</th><th>credits</th>";
				result +="</tr>";
				result +="</table>";
				result +="</div>";
			}
		}
		return result;
	}
	
	@MessageMapping("/manage-course-info")
    @SendTo("/client/manage-course-info")
    public String managing_course_info(String message) throws Exception {
		System.out.println("course info:"+message);
		Connection conn=DBAccesses.setupDB();
		Statement stmt=conn.createStatement();
		ResultSet rs=stmt.executeQuery("select CourseID,Credits from Course where CourseID= '"+message+"'");
		String result="";
//		result+=rs.getString("CourseID");
//		result+=" "+rs.getString("Credits");
		while(rs.next()){
//			result+="<tr>";
//			result+="<td>";
			System.out.println(rs.getRow());
			result+=rs.getString("CourseID");
//			result+="</td>";
//			result+="<td>";
			result+=" "+rs.getString("Credits");
//			result+="</td>";
//			result+="</tr>";
		}
		return result;
	}
}
