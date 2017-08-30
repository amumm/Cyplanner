package webcrawling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * A utility class to make connecting to, pulling from, and writing to
 * our database easier and to stop the duplication of code. 
 * @author jacob
 *
 */
public class DBAccesses {
	protected static int addToCourseTable(ArrayList<Course> arr,Connection conn){
		int i=0;	
		for (Course course : arr) {
			try{
				course.executeCourseUpdate(conn);
				i++;
			}catch(SQLException e) {
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
		return i;
	}
	
	/**
	 * Connects to our database
	 * @return a connection to the database
	 */
	public static Connection setupDB(){
		Connection conn=null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load driver.");
			e.printStackTrace();
		}
		try {
			String dbUrl = "jdbc:mysql://mysql.cs.iastate.edu:3306/db309ss1";
			String user = "dbu309ss1";
			String pass = "NzdkYjhkMTg5";
			// dbu309ss1@mysql.cs.iastate.edu:3306
			conn = DriverManager.getConnection(dbUrl, user, pass);
			System.out.println("***Connected to DB***");
//			conn.close();
		}catch(SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
		return conn;
	}
	/**
	 * creates a new table in our database. column_names, column_types, and arguments are 
	 * parallel arrays; i.e. column_names[3] is of type column_types[3] and has the arguments 
	 * arguments[3]. The arguments are optional, and if a column doesn't have any, set the 
	 * arguments at that index to an empty string. pkey is the primary key(if there is one).
	 * If there is not primary key, again, make it an empty string.
	 * @param tablename
	 * @param column_names
	 * @param column_types
	 * @param arguments
	 * @param pkey
	 * @param conn
	 * @return the query that ran on the database as a string
	 * @throws SQLException 
	 */
	public static String createTable(String tablename,String[] column_names, String[] column_types, String[] arguments,String ending_args, String pkey, Connection conn, boolean debug) throws SQLException{
		String query="create table ";
		query+=tablename;
		query+="( ";
		for (int i = 0; i < column_names.length; i++) {
			query+=column_names[i]+" ";
			query+=column_types[i]+" ";
			query+=arguments[i]+", ";
		}
		if(!pkey.equals(""))
			query+="primary key("+pkey+")";
		query+=ending_args;
		query+=");";
//		System.out.println(query);
		if(!debug){
			Statement stmt=conn.createStatement();
			stmt.executeUpdate(query);
		}
		return query;
	}
	
	public static void dropTable(String tablename,Connection conn) throws SQLException{
		Statement stmt=conn.createStatement();
		stmt.executeUpdate("drop table "+tablename);
	}
	/**
	 * Takes the ArrayList of Strings arr, and adds them to the table <program_name>_<program_type>
	 * in the database that conn is connected to. 
	 * @param program_name Name of the program you want to add a class to; ex: Software_Engineering
	 * @param program_type Type of the program you want to add a class to; ex: major
	 * @param arr List of strings to add to the program
	 * @param conn connection to our database
	 */
	public static void addToProgramTable(String program_name,String program_type,ArrayList<String> arr, Connection conn){
		for (String course : arr) {
			try{
				executeProgramUpdate(course,conn,program_name,program_type);
			}catch(SQLException e) {
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
	}
	
	private static void executeProgramUpdate(String courseName, Connection conn, String program_title, String program_type) throws SQLException{
		Statement stmt=conn.createStatement();
		stmt.executeUpdate("insert into " +program_title+"_"+program_type +" values('"+courseName +"');");
		
		stmt.close();
	}
	/**
	 * A course object containing all fields in the Course table of our database. 
	 * This is only really used when populating the Course table or reading from it
	 * @author jacob
	 *
	 */
	public static class Course{
		private String ID;
		private String dept;
		private String credits;
		private int avgDifRating;
		private int totalDifRating;
		private int avgWorkloadRating;
		private int totalWorkLoadRating;
		private String Desc;
		private String title;
		/**
		 * initializes the Course object with all the necessary fields
		 * @param ID
		 * @param dept
		 * @param credits
		 * @param avgDifRating
		 * @param totalDifRating
		 * @param avgWorkloadRating
		 * @param totalWorkLoadRating
		 * @param Desc
		 * @param title
		 */
		public Course(String ID,String dept,String credits,int avgDifRating,int totalDifRating,int avgWorkloadRating,int totalWorkLoadRating, String Desc,String title){
			this.ID=ID;
			this.dept=dept;
			this.credits=credits;
			this.avgDifRating=avgDifRating;
			this.totalDifRating=totalDifRating;
			this.avgWorkloadRating=avgWorkloadRating;
			this.totalWorkLoadRating=totalWorkLoadRating;
			this.Desc=Desc;
			this.title=title;
		}
		
		/**
		 * adds this course object to the database which conn is connected to
		 * @param conn
		 * @throws SQLException
		 */
		public void executeCourseUpdate(Connection conn) throws SQLException{
			Statement stmt=conn.createStatement();
			stmt.executeUpdate("insert into Course values('"+ID +"','"+ dept +"','"+ credits +"',"+avgDifRating +","+totalDifRating+","+avgWorkloadRating+","+totalWorkLoadRating+",'"+Desc+"', '"+title+"');");
			stmt.close();
		}
	}
	public static int addToPrereqTable(String courseId, String prereq, String equivalents,Connection conn) {
		try {
			Statement stmt=conn.createStatement();
			stmt.executeUpdate("insert into Prerequisites values('"+courseId +"','"+ prereq +"','"+ equivalents +"');");
			stmt.close();
			return 1;
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("failed on course: "+courseId+"'s prereq: "+prereq+"  equivalents: "+equivalents);
			return 0;
		}
	}
	
	public static int addToSingleColumnTable(String courseId, String table_name, Connection conn) {
		try {
			Statement stmt=conn.createStatement();
			stmt.executeUpdate("insert into "+table_name+" values('"+courseId +"');");
			stmt.close();
			return 1;
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("failed on course: "+courseId+ " in the "+table_name+" table");
			return 0;
		}
	}
	
}
