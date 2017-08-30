package webcrawling;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator.Tag;

import webcrawling.DBAccesses.Course;

/**
 * This class is for all webcrawling. It is currently primarily used for
 * populating tables.
 * @author jake
 *
 */
public class Webcrawler {
	public static void main(String[] args) throws IOException, SQLException {
		Connection conn=DBAccesses.setupDB();
//		populateCourses(conn);
//		updatePrereqTable("http://catalog.iastate.edu/azcourses/s_e/", "", conn);
//		updateMajorTable("http://catalog.iastate.edu/collegeofengineering/softwareengineering/", "SE", conn);

//		createProgramTables(conn);
//		populatePrereqTable(conn);

		populateGenEdTables(conn);

//		dropProgramTables();
		System.out.println("disconnected from db");
		conn.close();

	}

	/**
	 * A quick way to drop all the program tables, since we won't be able to properly fill them all.
	 * We'll have to start with one Major and work our way from there.
	 * @param conn
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void dropProgramTables(Connection conn) throws IOException, SQLException{
		Document doc=Jsoup.connect("http://catalog.iastate.edu/undergraduatemajors-alpha/").get();
		Element container=doc.getElementById("majorstextcontainer");
		Elements majors=container.getElementsByTag("li");

		ArrayList<String> tableNames=new ArrayList<String>();
		PrintWriter p=new PrintWriter("src/test/webcrawling_programs/deletion");
//		p.println("Tables");
		for (int i=0;i<majors.size();i++) {
			tableNames.add(majors.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_major");
		}
		container=doc.getElementById("minorstextcontainer");
		Elements minors=container.getElementsByTag("li");

//		p.println("Minors");
		for (int i=0;i<minors.size();i++) {
			tableNames.add(minors.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_minor");
		}
		container=doc.getElementById("certificatestextcontainer");
		Elements certificates=container.getElementsByTag("li");
//		p.println("Certificates");
		for (int i=0;i<certificates.size();i++) {
			tableNames.add(certificates.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_certificate");
		}
//		Connection conn=DBAccesses.setupDB();
		for(int i=0;i<tableNames.size();i++){
			try{
//			p.println("drop table "+tableNames.get(i));
			DBAccesses.dropTable(tableNames.get(i), conn);
			}
			catch(SQLException e){
				System.out.println(tableNames.get(i)+" failed");
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
		p.close();
	}

	/**
	 * This method takes a connection to a database as a parameter, then creates
	 * all of our "program" tables. It creates over 200 tables: one for every major, minor, and
	 * certificate program at Iowa State. The data for naming these tables comes from Iowa State's
	 * catalog webpage. Each table is named in the format <program name>_<program type>, for example:
	 * Software_Engineering_major or Computer_Science_minor.
	 * @param conn
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void createProgramTables(Connection conn) throws IOException, SQLException{

		PrintWriter p=new PrintWriter("src/test/webcrawling_programs/creation");
		createMajorTables(conn,p);
		createMinorTables(conn,p);
		createCertificateTables(conn,p);
		p.close();
	}

	private static void createMajorTables(Connection conn, PrintWriter p) throws IOException, SQLException{
		Document doc=Jsoup.connect("http://catalog.iastate.edu/undergraduatemajors-alpha/").get();
		Element container=doc.getElementById("majorstextcontainer");
		Elements majors=container.getElementsByTag("li");

		ArrayList<String> tableNames=new ArrayList<String>();
		String[] column_names=new String[1],column_types=new String[1],arguments=new String[1];
		String ending_args="";

//		p.println("Majors");
		int index=0;
		for (int i=0;i<majors.size();i++) {
			if((index>0 && i>0 && tableNames.get(index-1)!=majors.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_major")||index==0){
				tableNames.add(majors.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_major");
				column_names[0]="CourseID";
				column_types[0]="varchar(100)";
				arguments[0]="not null";
				ending_args="constraint fk_"+tableNames.get(index)+" foreign key("+column_names[0]+") references Course(CourseID)";
				try{
//				p.println(DBAccesses.createTable(tableNames.get(index), column_names, column_types,arguments,ending_args, "", conn, false));
				DBAccesses.createTable(tableNames.get(index), column_names, column_types,arguments,ending_args, "", conn, false);
				}catch(SQLException e){
					System.out.println(tableNames.get(i)+" failed in creation");
					System.out.println("SQLException: " + e.getMessage());
					System.out.println("SQLState: " + e.getSQLState());
					System.out.println("VendorError: " + e.getErrorCode());
				}
//				p.println(tableNames.get(index));

				index++;
				//			p.println(tableNames.get(i));
			}
		}

	}

	private static void createMinorTables(Connection conn,PrintWriter p) throws IOException, SQLException{
		Document doc=Jsoup.connect("http://catalog.iastate.edu/undergraduatemajors-alpha/").get();
		Element container=doc.getElementById("minorstextcontainer");
		Elements minors=container.getElementsByTag("li");

		ArrayList<String> tableNames=new ArrayList<String>();
		String[] column_names=new String[1],column_types=new String[1],arguments=new String[1];
		String ending_args="";
//		p.println("Minors");
		int index=0;
		for (int i=0;i<minors.size();i++) {
			if((index>0 && i>0 && tableNames.get(index-1)!=minors.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_major")||index==0){
				tableNames.add(minors.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_minor");
				column_names[0]="CourseID";
				column_types[0]="varchar(100)";
				arguments[0]="not null";
				ending_args="constraint fk_"+tableNames.get(i)+" foreign key("+column_names[0]+")";
				try{
//				p.println(DBAccesses.createTable(tableNames.get(i), column_names, column_types, arguments,ending_args, "", conn, false));
				DBAccesses.createTable(tableNames.get(i), column_names, column_types, arguments,ending_args, "", conn, false);
				}catch(SQLException e){
					System.out.println(tableNames.get(i)+" failed in creation");
					System.out.println("SQLException: " + e.getMessage());
					System.out.println("SQLState: " + e.getSQLState());
					System.out.println("VendorError: " + e.getErrorCode());
				}
				index++;
			}
		}
	}

	private static void createCertificateTables(Connection conn,PrintWriter p) throws IOException, SQLException{
		Document doc=Jsoup.connect("http://catalog.iastate.edu/undergraduatemajors-alpha/").get();
		Element container=doc.getElementById("certificatestextcontainer");
		Elements certificates=container.getElementsByTag("li");

		ArrayList<String> tableNames=new ArrayList<String>();
		String[] column_names=new String[1],column_types=new String[1],arguments=new String[1];
		String ending_args="";
//		p.println("Certificates");
		int index=0;
		for (int i=0;i<certificates.size();i++) {
			if((index>0 && i>0 && tableNames.get(index-1)!=certificates.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_major")||index==0){
				tableNames.add(certificates.get(i).text().split(",")[0].replace(" ", "_").replace(".", "").replace(":", "").replace("’", "").replace("/", "_").replace("-", "_")+"_certificate");
	//			p.println(tableNames.get(i));
				column_names[0]="CourseID";
				column_types[0]="varchar(100)";
				arguments[0]="not null";
				ending_args="constraint fk_"+tableNames.get(i)+" foreign key("+column_names[0]+")";
				try{
//					p.println(DBAccesses.createTable(tableNames.get(i), column_names, column_types, arguments,ending_args, "", conn, false));
					DBAccesses.createTable(tableNames.get(i), column_names, column_types, arguments,ending_args, "", conn, false);
				}catch(SQLException e){
					System.out.println(tableNames.get(i)+" failed in creation");
					System.out.println("SQLException: " + e.getMessage());
					System.out.println("SQLState: " + e.getSQLState());
					System.out.println("VendorError: " + e.getErrorCode());
				}
				index++;
			}
		}
	}
	/**
	 * This method webcrawls the Iowa State catalog website and
	 * pulls the title, department, number of credits, and description
	 * of every single course that Iowa State offers, then adds it to our database.
	 * @param conn: a connection to our database is required. This way, you only connect to the database once.
	 * @throws IOException
	 */
	public static int[] populateCourses(Connection conn) throws IOException{
		Document doc=Jsoup.connect("http://catalog.iastate.edu/azcourses/").get();
		Element index=doc.getElementById("atozindex");

		Elements pages = index.getElementsByTag("li");

		ArrayList<String> links = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();

		PrintWriter p=new PrintWriter("src/test/webcrawling_courses/populate.html");
		Elements tempEl;
		String[] temp;
		for (Element e: pages) {
			tempEl=e.getElementsByTag("a");
			if(!tempEl.attr("abs:href").equals("http://catalog.iastate.edu/azcourses/")){
				links.add(tempEl.attr("abs:href"));
				temp = e.text().split("\\(");
				names.add(temp[1].replaceAll(" ", "").replaceAll("\\)", "")+"test.html");
			}
		}

		int[] result=new int[2];
		int[] temp_ints=new int[2];
		for (int i =0;i<links.size();i++) {
//			p.println(links.get(i));
//			p.println(names.get(i));
			temp_ints=updateCourses(links.get(i), names.get(i),conn);
			result[0]+=temp_ints[0];
			result[1]+=temp_ints[1];
//			updateMajorTable(links.get(i), names.get(i),conn);
		}

		p.close();
		return result;
	}

	private static int[] updateCourses(String url,String outFile,Connection conn) throws IOException{

		Document doc=Jsoup.connect(url).get();

		Element courseInventory=doc.getElementById("courseinventorycontainer");
		Elements courses=courseInventory.getElementsByClass("courseblock");

		ArrayList<String> titles=new ArrayList<String>();
		ArrayList<String> credits=new ArrayList<String>();
		ArrayList<String> depts=new ArrayList<String>();
		ArrayList<String> descriptions=new ArrayList<String>();
		ArrayList<String> titlesOfCourses=new ArrayList<String>();


		String[] tempCreditsArr=new String[10];
		String[] tempTitleArr=new String[2];

		for (Element element : courses) {
			element.html(element.html().replaceAll("&nbsp;", ""));

			//get titles
			tempTitleArr=element.getElementsByClass("courseblocktitle").text().split(":");
			titles.add(tempTitleArr[0]);
			titlesOfCourses.add(tempTitleArr[1]);

			//get depts
			depts.add(element.getElementsByClass("courseblocktitle").text().split("[^A-Za-z]")[0]);
			//get credits
			tempCreditsArr=element.getElementsByClass("credits noindent").text().split(" ");
			for (int i=0;i<tempCreditsArr.length;i++) {
				if(tempCreditsArr[i].contains("Cr.")){
					i++;
					credits.add(tempCreditsArr[i].replace(".", ""));
					break;
				}
			}
			//get description
			descriptions.add(element.getElementsByClass("prereq").text().replaceAll("'", ""));

		}

		ArrayList<Course> courselist=new ArrayList<Course>();

		PrintWriter p=new PrintWriter("src/test/webcrawling_courses/"+outFile);
//		p.println("Course Titles and Credits\n");
		for(int i=0;i<titles.size();i++){
//			p.println(titles.get(i));
//			p.println(depts.get(i));
//			p.println(credits.get(i));
//			p.println(descriptions.get(i));
//			p.println(titlesOfCourses.get(i));

			courselist.add(new Course(titles.get(i),depts.get(i),credits.get(i),0,0,0,0,descriptions.get(i),titlesOfCourses.get(i).replaceAll("'", "")));

		}
//		p.println("\n");

		p.close();
		System.out.println(outFile);
		int[] result=new int[2];
		result[0]=courselist.size();
		result[1]=DBAccesses.addToCourseTable(courselist,conn);
		return result;
	}

	private static void updateMajorTable(String url, String major,Connection conn) throws IOException{
		Document doc=Jsoup.connect(url).get();

		Elements courseTable=doc.getElementsByClass("sc_plangrid");
		Elements course=courseTable.get(0).getElementsByClass("codecol");
		ArrayList<String> coursenames=new ArrayList<String>();

		int location;
		String sub = new String("");
		Elements text = course.select("a");
		String pattern = "[A-Za-z]";
		Pattern r = Pattern.compile(pattern);
		Matcher m;
		for (Element e : text) {
			e.html(e.html().replaceAll("&nbsp;", ""));
			m = r.matcher(e.text());

			if(!m.find()) continue; //No Letters
			if(e.text().contains("Elective")) continue; //just an elective
			if(e.text().contains("CPR E ")) continue; // optional course
			if(e.text().contains("or")){ //remove other optional courses
				location = e.text().indexOf("or");
				sub = e.text().substring(0, location);
				coursenames.add(sub);
			}

			else{ //otherwise just add it as normal
				coursenames.add(e.text());
			}

		}

		PrintWriter p=new PrintWriter("src/test/webcrawling_programs/"+major);
		for (int i = 0; i < coursenames.size(); i++) {
//			p.println(coursenames.get(i));
		}
		p.close();
		DBAccesses.addToProgramTable(major,"major",coursenames,conn);
	}
	/**
	 * Fills the prerequisite table by parsing through the course descriptions on the
	 * ISU catalog page. This takes a while to run.
	 * @param conn
	 * @throws IOException
	 */
	public static int[] populatePrereqTable(Connection conn) throws IOException{
		Document doc=Jsoup.connect("http://catalog.iastate.edu/azcourses/").get();
		Element index=doc.getElementById("atozindex");

		Elements pages = index.getElementsByTag("li");

		ArrayList<String> links = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();

		PrintWriter p=new PrintWriter("src/test/webcrawling_prereq/prerequisite.html");
		Elements tempEl;
		String[] temp;
		for (Element e: pages) {
			tempEl=e.getElementsByTag("a");
			if(!tempEl.attr("abs:href").equals("http://catalog.iastate.edu/azcourses/")){
				links.add(tempEl.attr("abs:href"));
				temp = e.text().split("\\(");
				names.add(temp[1].replaceAll(" ", "").replaceAll("\\)", "")+"_prereq.html");
			}
		}
		int[] result=new int[2];
		int[] temp_ints=new int[2];
		for (int i =0;i<links.size();i++) {
//			p.println(links.get(i));
//			p.println(names.get(i));
			temp_ints=updatePrereqs(links.get(i), names.get(i),conn);
			result[0]+=temp_ints[0];
			result[1]+=temp_ints[1];

		}

		p.close();
		return result;
	}

	private static int[] updatePrereqs(String url,String outFile,Connection conn) throws IOException{

		Document doc=Jsoup.connect(url).get();

		Element courseInventory=doc.getElementById("courseinventorycontainer");
		Elements courses=courseInventory.getElementsByClass("courseblock");

		ArrayList<String> courseIds=new ArrayList<String>();
		ArrayList<ArrayList<String>> prereqs=new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> prereqEquivalents=new ArrayList<ArrayList<String>>();

		String[] tempTitleArr=new String[2];

		Elements prereqElements;
		Elements e2=new Elements();
		ArrayList<String> temp_prereq_array;
		String tempString;
		ArrayList<String> prereqEquivalent;
		String[] splitArray;
		String toAdd;
		final Pattern pattern = Pattern.compile("^[A-Z0-9]+$");
		final Pattern patternAllCaps = Pattern.compile("^[A-Z]+$");
		final Pattern patternAllNums = Pattern.compile("^[0-9]+$");
		final Pattern containsNums = Pattern.compile(".*[0-9].*");
		final Pattern containsCaps = Pattern.compile(".*[A-Z].*");
		boolean lastWasOr;

		for (Element element : courses) {
			element.html(element.html().replaceAll("&nbsp;", ""));

			//get titles
			tempTitleArr=element.getElementsByClass("courseblocktitle").text().split(":");
			courseIds.add(tempTitleArr[0]);
			//get description
			prereqElements=element.getElementsByClass("prereq");//should only be one

			temp_prereq_array=new ArrayList<String>();
			for (Element e : prereqElements) {
				e2=e.getElementsByTag("em");
			}
			tempString=e2.text().replaceAll(";", "").replace(".", "").replace(",", "").replace("/", " or ");
			if(tempString.contains("Prereq: ")){
				tempString=tempString.replace("Prereq: ", "");
			}
			splitArray=tempString.split(" ");
			prereqEquivalent=new ArrayList<String>();
			for(int i=0;i<splitArray.length;i++){
				if(pattern.matcher(splitArray[i]).matches()){//if you find a course id, add all its equivalents

					//this if/else block just changes A B E 271 -> ABE271
					if(patternAllCaps.matcher(splitArray[i]).matches()){
						toAdd="";
						while(i<splitArray.length&&(patternAllCaps.matcher(splitArray[i]).matches()||patternAllNums.matcher(splitArray[i]).matches())){
							toAdd+=splitArray[i];
							i++;
							if(patternAllNums.matcher(splitArray[i-1]).matches()){//go until you find the number of the class
								break;
							}
						}
					}
					else {
						toAdd=splitArray[i];
						i++;
					}
					if(containsCaps.matcher(toAdd).matches()&&containsNums.matcher(toAdd).matches())
						temp_prereq_array.add(toAdd);
					else{
						continue;
					}
//					i++;
					toAdd="";

					//add equivalents of this prereq
					if(i<splitArray.length&&splitArray[i].equals("or")){
						lastWasOr=true;
						while(i<splitArray.length&&(pattern.matcher(splitArray[i]).matches()||splitArray[i].equals("or"))){//continues while it's a course id or the word or
							if(splitArray[i].equals("or")){
								i++;
								lastWasOr=true;
								continue;
							}
							if(!lastWasOr){//goes until there aren't any more or's
								i--;
								break;
							}
							lastWasOr=false;
							tempString="";
							if(patternAllCaps.matcher(splitArray[i]).matches()){//starts on all caps
								while(i<splitArray.length&&(patternAllCaps.matcher(splitArray[i]).matches()||patternAllNums.matcher(splitArray[i]).matches())){
									tempString+=splitArray[i];
									i++;
									if(patternAllNums.matcher(splitArray[i-1]).matches()){//ends on all number
										i--;
										break;
									}
								}
							}
							else tempString=splitArray[i];

							if(containsCaps.matcher(tempString).matches()&&containsNums.matcher(tempString).matches()){
								toAdd+=tempString+" ";
							}
							i++;
						}
					}else i--;//make sure the cursor doesn't skip something that's not 'or'
					prereqEquivalent.add(toAdd);

				}
			}
			prereqs.add(temp_prereq_array);
			prereqEquivalents.add(prereqEquivalent);
		}

		int[] result=new int[2];
		result[0]=courseIds.size();
		result[1]=0;

		PrintWriter p=new PrintWriter("src/test/webcrawling_prereq/"+outFile);
//		p.println("Course Titles and Credits\n");
		for(int i=0;i<courseIds.size();i++){
//			p.println(courseIds.get(i));
			for (int j=0;j<prereqs.get(i).size();j++) {
//				p.print(courseIds.get(i)+ " ");
//				p.print("prereq: ");
//				p.print(prereqs.get(i).get(j));
//				p.print(" equivalent: ");
//				p.println(prereqEquivalents.get(i).get(j));
				result[1]+=DBAccesses.addToPrereqTable(courseIds.get(i),prereqs.get(i).get(j),prereqEquivalents.get(i).get(j),conn);
			}
//			p.println();
//			p.print("equivalents: ");

//			courselist.add(new Course(courseIds.get(i),depts.get(i),credits.get(i),0,0,0,0,descriptions.get(i),titlesOfCourses.get(i).replaceAll("'", "")));

		}
//		p.println("\n");

		p.close();
		System.out.println(outFile);
//		DBAccesses.addToCourseTable(courselist,conn);
		return result;
	}
	public static int[] populateUSDiversityTable(Connection conn) throws IOException{
		Document doc=Jsoup.connect("http://www.registrar.iastate.edu/students/div-ip-guide/usdiversity-courses").get();
		Element content=doc.getElementById("node-38");
		content=content.getElementsByClass("field-item even").first();
		content.html(content.html().replaceAll("&nbsp;", " ").replaceAll("&amp;", ""));
		content.select("a").remove();

//		final Pattern pattern = Pattern.compile("^[A-Z0-9]+$");
		final Pattern patternAllCaps = Pattern.compile("^[A-Z]+$");
		final Pattern patternAllNums = Pattern.compile("^[0-9]+$");
		final Pattern containsNums = Pattern.compile(".*[0-9].*");
//		final Pattern containsCaps = Pattern.compile(".*[A-Z].*");


		ArrayList<String> courses=new ArrayList<String>();
		ArrayList<String> contents_split=new ArrayList<String>();
		String[] words;
		String toAdd="";
		int i;
		Elements contents=content.getElementsByTag("p");

		//this page isn't formatted very well, so you need to split paragraphs up by <br> for this to work
		for (Element elem : contents) {
			words=elem.html().split("<br>");
			for (int j = 0; j < words.length; j++) {
				contents_split.add(words[j].trim());
			}
		}


		for (int j=0; j<contents_split.size();j++) {
			words=contents_split.get(j).split(" ");
			toAdd="";
			i=0;
			while(i<words.length&&(patternAllCaps.matcher(words[i]).matches()||patternAllNums.matcher(words[i]).matches())){
				if(patternAllCaps.matcher(words[i]).matches()){
					toAdd+=words[i];
					i++;
				}else if(containsNums.matcher(words[i]).matches()){
					toAdd+=words[i];
					courses.add(toAdd);
					toAdd="";
					i++;
				}
			}

		}
		int[] result=new int[2];
		result[0]=courses.size();
		result[1]=0;
		PrintWriter p=new PrintWriter("src/test/webcrawling_general/USDiversity.html");
		for (String course : courses) {
			result[1]+=DBAccesses.addToSingleColumnTable(course, "US_Diversity", conn);
//			p.println(course);
		}
		p.close();
		return result;
	}
	public static int[] populateInternationalPerspectiveTable(Connection conn) throws IOException{
		Document doc=Jsoup.connect("http://www.registrar.iastate.edu/students/div-ip-guide/IntlPerspectives-current").get();
		Element content=doc.getElementById("node-119");
		content=content.getElementsByClass("field-item even").first();
		content.html(content.html().replaceAll("&nbsp;", " ").replaceAll("&amp;", ""));
		content.select("a").remove();

//		final Pattern pattern = Pattern.compile("^[A-Z0-9]+$");
		final Pattern patternAllCaps = Pattern.compile("^[A-Z]+$");
		final Pattern patternAllNums = Pattern.compile("^[0-9]+$");
		final Pattern containsNums = Pattern.compile(".*[0-9].*");
//		final Pattern containsCaps = Pattern.compile(".*[A-Z].*");


		ArrayList<String> courses=new ArrayList<String>();
		ArrayList<String> contents_split=new ArrayList<String>();
		String[] words;
		String toAdd="";
		int i;
		Elements contents=content.getElementsByTag("p");

		//this page isn't formatted very well, so you need to split paragraphs up by <br> for this to work
		for (Element elem : contents) {
			words=elem.html().split("<br>");
			for (int j = 0; j < words.length; j++) {
				contents_split.add(words[j].trim());
			}
		}


		for (int j=0; j<contents_split.size();j++) {
			words=contents_split.get(j).split(" ");
			toAdd="";
			i=0;
			while(i<words.length&&(patternAllCaps.matcher(words[i]).matches()||patternAllNums.matcher(words[i]).matches())){
				if(patternAllCaps.matcher(words[i]).matches()){
					toAdd+=words[i];
					i++;
				}else if(containsNums.matcher(words[i]).matches()){
					toAdd+=words[i];
					courses.add(toAdd);
					toAdd="";
					i++;
				}
			}

		}
		int[] result=new int[2];
		result[0]=courses.size();
		result[1]=0;
		PrintWriter p=new PrintWriter("src/test/webcrawling_general/InternationalPerspective.html");
		for (String course : courses) {
			result[1]+=DBAccesses.addToSingleColumnTable(course, "International_Perspective", conn);
//			p.println(course);
		}
		p.close();
		return result;
	}
	public static int[] populateGenEdTables(Connection conn) throws IOException{

		Document doc=Jsoup.connect("https://las.iastate.edu/students/academics/general-education/general-education-approved-course-list-2017-18/#III.%20Social%20Sciences").get();
		Element contents=doc.getElementsByClass("tabbed").first();
		contents.html(contents.html().replaceAll("&nbsp;", " ").replaceAll("&amp;", "").replaceAll("\'", ""));
		Elements sections =contents.getElementsByTag("section");
		ArrayList<String> section_titles=new ArrayList<String>();

		Elements section_headers=contents.getElementsByTag("h1");
		for (Element element : section_headers) {
			section_titles.add(element.text().split("\\.")[1].trim().replaceAll(" ", "_"));
		}

		PrintWriter p=new PrintWriter("src/test/webcrawling_general/GenEds.html");

//		p.println("sections:  ");
		int[] result=new int[2];
		int[] temp_ints=new int[2];
		for (int i = 0; i < section_titles.size(); i++) {
//			p.println(section_titles.get(i));
			temp_ints=updateGenEds(section_titles.get(i),sections.get(i),p,conn);
			result[0]+=temp_ints[0];
			result[1]+=temp_ints[1];
		}
		p.close();
		return result;
		
	}
	private static int[] updateGenEds(String table_name,Element section, PrintWriter p, Connection conn){
		Elements rows=section.getElementsByTag("tr");
		Elements row_data;
		String row_dept;
		Element row_class_nums;
		String nums[];
//		p.println(table_name);
//		p.println();
		int[] result=new int[2];
		result[0]=0;//total found
		result[1]=0;//total added
		for (Element row : rows) {
			row_data=row.getElementsByTag("td");
			row_dept=row_data.first().text().split("\\(")[1].replaceAll("\\)", "").replaceAll(" ", "");
//			p.println(row_dept);
			row_class_nums=row_data.last();
			nums=row_class_nums.text().replaceAll("[A-Z]", "").split("\\, ");
			for (int i = 0; i < nums.length; i++) {
				result[0]+=1;
				result[1]+=DBAccesses.addToSingleColumnTable(row_dept+nums[i], table_name, conn);
//				p.println(row_dept+nums[i]);
			}
		}

		
		
		

//		for (Element row : rows) {
//			p.println(row);
//		}
		return result;
	}

}
