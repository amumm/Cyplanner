package controllers;
import java.util.ArrayList;

/**
 * 
 * @author Andrew Mumm
 *an object representation of a class that is used or topological sort
 */
public class CourseSort {
	
	/**
	 * A list of preReqs for a course
	 */
	private ArrayList<CourseSort> preReqs;
	
	/**
	 * A list of completed preReqs for a course
	 */
	public ArrayList<CourseSort> compPreReqs;
	
	/**
	 * the name of a course
	 */
	private String name;
	
	/**
	 * the number of preReqs for a course
	 */
	private int numPreReqs;
	
	/**
	 * the Program for a course
	 */
	private String program;
	
	/**
	 * the number of credits for a course
	 */
	private String credits;
	
	/**
	 * The constructor for the Course Class
	 * @param name
	 * takes a string argument to be used as the name of the course
	 */
	public CourseSort (String name, String program, String credits){
		this.setProgram(program);
		this.setName(name);
		this.setCredits(credits);
		numPreReqs = 0;
		this.preReqs = new ArrayList<CourseSort>();
		this.compPreReqs = new ArrayList<CourseSort>();
		
	}
	
	/**
	 * Adds a preReq to a courses preReq list
	 * @param preReq
	 * the preReq to be added
	 */
	public void addPreReq(CourseSort preReq){
		
		getPreReqs().add(preReq);
		numPreReqs++;
		
	}
	
	/**
	 * Removes a preReq from a courses preReq list
	 * @param preReq
	 * the preReq to be removed
	 */
	public void removePreReq(CourseSort preReq){
		while(getPreReqs().contains(preReq)){
			getPreReqs().remove(preReq);
			numPreReqs--;
		}
	}
	
	/**
	 * Returns the number of preReqs for a course
	 * @return numPreReqs
	 * the number of preReqs for a course
	 */
	public int getNumPreReqs(){
		return numPreReqs;
	}

	public ArrayList<CourseSort> getPreReqs() {
		return preReqs;
	}

	public void setPreReqs(ArrayList<CourseSort> preReqs) {
		this.preReqs = preReqs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getCredits() {
		return credits;
	}

	public void setCredits(String credits) {
		this.credits = credits;
	}

	public ArrayList<CourseSort> getCompPreReqs() {
		return compPreReqs;
	}

	public void setCompPreReqs(ArrayList<CourseSort> compPreReqs) {
		this.compPreReqs = compPreReqs;
	}
}
