/**
 * @author Andrew Mumm
 */
var courses = [];
var stompClient = null;
var isConnected = false;

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
        stompClient.subscribe('/client/SignUp_ValidReturn', function (managing) {
        	result = managing.body;
        	if(result == "success"){
	        	var message = document.getElementById("userName").value + " ";
	    		for(var i = 0; i < courses.length; i ++){
	    			message += courses[i] + " ";
	    		}
	    		addCourses(message);
        	}	
        	else
        		$(".failure").html("username is not available");
        });
        
        stompClient.subscribe('/client/Signup_AutoCompReturn', function (managing) {
        	var which = managing.body.split(", ");
        	var list = which.slice(0, which.lenth);
        	console.log(list);
    		$( "#completed" ).autocomplete({
    			source: list
    		});
        });
        
        stompClient.subscribe('/client/Signup_addCompletedCoursesReturn', function (managing) {
        	var result = managing.body;
        	console.log(result);
        	if(result == "complete"){
        		sendToPage("Login");
        	}
        		
        });
    });
}

/**
 * sends the user name and password to the server side
 * @param userPass
 * user name and password
 * @returns
 */
function send(userPass){

  stompClient.send("/app/SignUp_Valid",{}, userPass);
}

/**
 * sends the user name and password to the server side
 * @param userPass
 * user name and password
 * @returns
 */
function addCourses(courses){

  stompClient.send("/app/Signup_addCompletedCourses",{}, courses);
}

function autoComp(input){
	
	stompClient.send("/app/Signup_AutoComp",{}, input);
}

/**
 * when the document loads the functions can be performed
 */
$(document).ready(function () {
	connect();
	
//	localStorage.setItem("courses", courses);
	/**
	 * gets the user name and password to send to the server side
	 */
    $(".sub").click(function () {
    	
    	if (credentialCheck()){
    		var username = document.getElementById("userName").value;
	    	var email = document.getElementById("email").value;
	    	var password = document.getElementById("password").value;
	    	var message = username + " " + email + " " + password;
	    	send(message);
    	}
    	
    });
    
    $("#completed").on('input', function(){
        autoComp($("#completed").val());
	});

    $("#add").click(function(){
    	var result = "";
    	courses[courses.length] = $("#completed").val().toString();
        $("#completed").val("");
        result += "<tr><th colspan= \"3\" >Courses</th></tr>";
        var i = 0;
        while(i < courses.length){
        	result += "<tr>";
        	result += "<td>" + courses[i] + "</td>";
        	i++;
        	if(i >= courses.length){
        		result += "</tr>";
        		break;
        	}
        	result += "<td>" + courses[i] + "</td>";
        	i++;
        	if(i >= courses.length){
        		result += "</tr>";
        		break;
        	}
        	result += "<td>" + courses[i] + "</td>";
        	i++;
        	$("#table").html($("#table").html() + "</tr>");
        }
        
        $("#table").html(result);
        
	});
});
