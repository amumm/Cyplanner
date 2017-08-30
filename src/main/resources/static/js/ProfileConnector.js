/**
 * @author Andrew Mumm
 */
var courses = [];
var courses3= [];
var index=0;
var stompClient = null;
var isConnected = false;
var addRem = 0;

/**
 * Connects the client side to the served side with the stomp client
 * This creates a connection between client side and server side 
 * and handles messaging between the two.
 * @returns
 */
function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    
    /**
     * performs the connection the server side
     */
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        isConnected == true;
        
        /**
         * handles the request sent back from the server and displays a message
         * on the client side
         */
        stompClient.subscribe('/client/Profile_addCompletedCoursesReturn', function (managing) {
        	result = managing.body;
        	console.log(result);
        });
        stompClient.subscribe('/client/Profile_removeCompletedCoursesReturn', function (managing) {
        	result = managing.body;
        	console.log(result+"also hi");
        	console.log(result);
        });
        stompClient.subscribe('/client/Profile_ManCoursesReturn', function (managing) {
        	result = managing.body;
        	console.log(result+"hi");
        	localStorage.setItem("courses", result);
        	localStorage.setItem("num_semesters",JSON.parse(result).length);
        	
        });
        stompClient.subscribe('/client/Profile_ManCreditsReturn', function (managing) {
        	result = managing.body.split(";;;");
        	console.log(result+"hi");
        	localStorage.setItem("credits", result[0]);
        	localStorage.setItem("program", result[1]);
        	localStorage.setItem("fromAuto", true);
        	if(document.URL.includes('localhost'))
        	    window.location.href = "http://localhost:8080/manage";
        	else
        	    window.location.href = "http://proj-309-ss-1.cs.iastate.edu:8080/manage";
        	
        });
        
        stompClient.subscribe('/client/Profile_AutoCompleteReturn', function (managing) {
        	var which = managing.body.split(", ");
        	var list = which.slice(0, which.lenth);
        	console.log(list);
        	if(addRem == 0){
        		$( "#completed" ).autocomplete({
        			source: list
        		});
        	}
        	else{
        		$( "#removed" ).autocomplete({
        			source: list
        		});
        	}
        });
        
        stompClient.subscribe('/client/Profile_updateFeildsReturn', function (managing) {
        	console.log(managing.body);
        	if($("#changeUserName").val() != ""){
        		$("#userName").html($("#changeUserName").val());
                document.getElementById('a').style.visibility = 'hidden';
                localStorage.setItem("user", $("#changeUserName").val()); 
                $("#changeUserName").val("");
        	}
        	
	        if($("#changeEmail").val() != ""){
	        	$("#Email").html($("#changeEmail").val());
		        document.getElementById('b').style.visibility = 'hidden';
		        localStorage.setItem("Email",  $("#changeEmail").val());
		        $("#changeEmail").val("");
	        }
            
        });
    });
}

function autoComp(input){
	
	stompClient.send("/app/Profile_AutoComp",{}, input);
}

function addCourses(courses){

	  stompClient.send("/app/Profile_addCompletedCourses",{}, courses);
}
function removeCourses(courses)
{
	stompClient.send("/app/Profile_removeCompletedCourses",{}, courses);

}
function changeUserFeilds(userName){

	  stompClient.send("/app/Profile_updateFeilds",{}, userName);
}
function setManCourses(schedule)
{
	stompClient.send("/app/Profile_ManCourses",{}, schedule);
}
function setManCredits(schedule)
{
	stompClient.send("/app/Profile_ManCredits",{}, schedule);

}
/**
 * gets data to be sent to the server side
 */
$(document).ready(function () {
	connect();
	
	$("#completed").on('input', function(){
		addRem = 0;
        autoComp($("#completed").val());
	});
	$("#removed").on('input', function(){
		addRem = 1;
        autoComp($("#removed").val());
	});

	//add function for remove courses
	$("#remove").click(function(){
		message = localStorage.getItem("user") + " ";
        	message += $("#removed").val();
        	var v = $("#removed").val();
		$("#removed").val("");
		
		buildCourseTable2(v);
//		localStorage.setItem("compCourses",courses3);
        	removeCourses(message);
	});
	
	$("#add").click(function(){
		var message = localStorage.getItem("user") + " ";
        	message += $("#completed").val();
		
		courses += $("#completed").val() + " ";
		var va = $("#completed").val();
		
		$("#completed").val("");
		
//		localStorage.setItem("compCourses", courses);
        	buildCourseTable(va);
		
        	addCourses(message);
	});
	
	$("#submitUserName").click(function(){
		if(!alphaNumCheck($("#changeUserName").val()))
            $(".error").html("Invalid Username Format");
        else{
        	var message = localStorage.getItem("user") + " ";
        	message += $("#changeUserName").val() + " ";
        	message += localStorage.getItem("Email");
        	changeUserFeilds(message);
        }
	});
	
	$("#submitEmail").click(function(){
	      if(!emailCheck($("#changeEmail").val()))
	        $(".error").html("Invalid Email Format");
	      else{
	    	  var message = localStorage.getItem("user") + " ";
	    	  message += localStorage.getItem("user") + " ";
	    	  message += $("#changeEmail").val();
	    	  changeUserFeilds(message);
	      }
	});
	
	
});
function getSchedule(schedule)
{
	setManCourses(schedule);
	setManCredits(schedule);
 	
}
function addSchedules(){
	var schedules = localStorage.getItem("schedules");
    var temp = schedules.split(", ");
    for(var i = 0; i < temp.length; i++){
      $("#scheds").html($("#scheds").html() + ' <span style = "cursor: pointer; margin-left: 2%;" onclick = "getSchedule(\''+temp[i]+'\')" id = \''+ temp[i] + '\' >' + temp[i] + '</span>');
    }
}

function buildCourseTable(va){
	  var result = "";
	  if(va == ""){
		  courses3 = localStorage.getItem("compCourses").split(" ");
		  courses3 = courses3.slice(0, courses3.length - 1);
		  index = courses3.length - 1;
	  }
	  else{
		  courses3[index] = va;
		  var toString = "";
		  for(var i = 0; i < courses3.length; i++){
			  toString += courses3[i] + " ";
		  }
		  localStorage.setItem("compCourses", toString);
	  }
	  
	  //courses2 = courses.split(" ");
	  
	  result += '<tr><th colspan= "3" >Courses</th></tr>';
	  var i = 0;
	  while(i <=(courses3.length - 1)){
		  
	  	result += "<tr>";
	  	if(courses3[i]!=null){
	  		result += "<td>" + courses3[i] + "</td>";
	  	}
	  	i++;
	  	if(i > courses3.length - 1){
	  		result += "</tr>";
	  		break;
	  	}
	  	if(courses3[i]!=null){
	  		result += "<td>" + courses3[i] + "</td>";
  		}
	  	i++;
	  	if(i > courses3.length - 1){
	  		result += "</tr>";
	  		break;
	  	}
	  	if(courses3[i]!=null){
	  		result += "<td>" + courses3[i] + "</td>";
  		}
	  	i++;
//	  	$("#table").html($("#table").html() + "</tr>");
	  	result += "</tr>";
		  
	  }
	  if(courses3[index]!= null){
		  index++;
	  }
	  $("#table").html(result);
	  
	 }

function buildCourseTable2(v){
	  var result = "";
	  var ind=courses3.indexOf(v);
	  var j=ind;
	  while(j<(courses3.length-1))
		  {
		  	if(j+1<=courses3.length-1)
		  		{
		  			courses3[j]=courses3[j+1];
		  		}
		  	j++;
		  
		  }
	  courses3[j]=null;
	  index=index-1;
	  var courses2 = new Array();
	  
	  result += '<tr><th colspan= "3" >Courses</th></tr>';
	  var i = 0;
	  while(i < (courses3.length - 1)){
		  
	  	result += "<tr>";
	  	if(courses3[i]!=null)
	  		{
	  	result += "<td>" + courses3[i] + "</td>";
	  	courses2.push(courses3[i]);
	  		}
	  	i++;
	  	if(i >= courses3.length - 1){
	  		result += "</tr>";
	  		break;
	  	}
	  	if(courses3[i]!=null)
  		{
	  	result += "<td>" + courses3[i] + "</td>";
	  	courses2.push(courses3[i]);
  		}
	  	i++;
	  	if(i >= courses3.length - 1){
	  		result += "</tr>";
	  		break;
	  	}
	  	if(courses3[i]!=null)
  		{
	  	result += "<td>" + courses3[i] + "</td>";
	  	courses2.push(courses3[i]);
  		}
	  	i++;
	  	$("#table").html($("#table").html() + "</tr>");
		  
	  }
	  
	  $("#table").html(result);
	  var toString = "";
	  for(var i = 0; i < courses2.length; i++){
		  toString += courses2[i] + " ";
	  }
	  localStorage.setItem("compCourses", toString);
	 }
function emailCheck(email) {
    atSplit = email.split('@');
    var valid = alphaNumCheck(atSplit[0]);
    if (atSplit.length == 2) {
      if(valid){
        periodSplit = atSplit[1].split('.');
        valid = alphaNumCheck(periodSplit[0] + periodSplit[1]);
        if (periodSplit.length == 2) {
          if(valid)return true;
        }
      }
    }
    return false;
  }
  function alphaNumCheck(entry) {
    let regex = /^[a-zA-Z0-9]+$/i;
    var valid = entry.match(regex);
    if (entry != null)
      if(valid) return true;
    else return false;
  }
