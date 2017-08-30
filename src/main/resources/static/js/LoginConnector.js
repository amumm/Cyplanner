/**
 * @author Andrew Mumm
 */

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
        stompClient.subscribe('/client/Login_ValidReturn', function (managing) {
        	result = managing.body.split(" ");
        	if(result[0] == "success"){
        		localStorage.setItem("Email", result[1]);
        		var username = $("#userName").val();
        		getCompCourses(username);
        	}
        	else{
        		$(".failure").html("invalid username and password");
        	}

        });
        
        stompClient.subscribe('/client/Login_getCompletedCoursesReturn', function (managing) {
        	result = managing.body.split(", ");
        	localStorage.setItem("compCourses", result);
        	var username = $("#userName").val();
        	getSchedules(username);
        });
        
        stompClient.subscribe('/client/Login_getSchedulesReturn', function (managing) {
//        	result = managing.body.split(", ");
        	result = managing.body;
        	localStorage.setItem("schedules", result);
        	
        	if(document.URL == "http://localhost:8080/Login")
                window.location.href = "http://localhost:8080/";
            else
            	window.location.href = "http://proj-309-ss-1.cs.iastate.edu:8080/"
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

  stompClient.send("/app/Login_Valid",{}, userPass);
}

/**
 * gets the completed courses
 * @param message
 * user name and password
 * @returns
 */
function getCompCourses(message){

  stompClient.send("/app/Login_getCompletedCourses",{}, message);
}

/**
 * gets the schedules
 * @param message
 * user name
 * @returns
 */
function getSchedules(message){

  stompClient.send("/app/Login_getSchedules",{}, message);
}

/**
 * when the document loads the functions can be performed
 */
$(document).ready(function () {
	connect();

	/**
	 * gets the user name and password to send to the server side
	 */
    $(".button1").click(function () {
    	var username = document.getElementById("userName").value;
    	var password = document.getElementById("password").value;
    	localStorage.setItem("user", username);
    	localStorage.setItem("pass", password);
    	var message = username + " " + password;
    	send(message);
    });

    /**
     * redirects the page to the sign-up page
     */
    $(".button2").click(function () {
      if(document.URL == "http://localhost:8080/Login"){
        window.location.href = "http://localhost:8080/SignUp";
      }
      else{
        window.location.href = "http://proj-309-ss-1.cs.iastate.edu:8080/SignUp"
      }
    });
});
