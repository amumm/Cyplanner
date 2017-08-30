package controllers;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Semester {
	
/**
 * number of credits from courses	
 */
private int credits;

/**
 * the semester number
 */
private int semesterNum;

/**
 * array for storing courses
 */
public ArrayList<CourseSort> courses;

	public Semester(int semesterNum){
		
		this.semesterNum = semesterNum;
		this.credits = 0;
		this.courses = new ArrayList<CourseSort>();
		
	}
	
	public int getSemNum(){
		return semesterNum;
	}
	
	public int getCredits(){
		return credits;
	}
	
	private void addCredits(CourseSort course){
		
		final Pattern patternAllNums = Pattern.compile("^[0-9]+$");
		final Pattern containsNums = Pattern.compile(".*[0-9].*");
		
		if(containsNums.matcher(course.getCredits()).matches()){
			if(patternAllNums.matcher(course.getCredits()).matches())
				credits += Integer.parseInt(course.getCredits());
			else
				credits += (int)(course.getCredits().charAt(0));
		}
	}
	
	public void addCourse(CourseSort course){
		courses.add(course);
		addCredits(course);
	}
		
}
