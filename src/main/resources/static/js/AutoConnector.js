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
        stompClient.subscribe('/client/add', function (managing) {
        	result = managing.body.split(" ");
        	if(result[1] == null){
        		$(".error").html("<p>invalid input</p>");
        	}
        	else{
    			if(result[0] == "Majors" && result.length > 2){ //more than one word in the name
    				var moreThanOne = document.getElementById("Major").innerHTML.split(" ");
    				if(moreThanOne.length > 1 && moreThanOne[1] != ""){
    					$(".major").html(document.getElementById("Major").innerHTML + ", " + result[1] + " " + result[2]);
    				}
    				else{
    					$(".major").html(document.getElementById("Major").innerHTML + result[1] + " " + result[2]);
    				}
        		}
    			else if(result[0] == "Majors"){ //only one word in the name
    				var moreThanOne = document.getElementById("Major").innerHTML.split(" ");
    				if(moreThanOne.length > 1 && moreThanOne[1] != ""){
    					$(".major").html(document.getElementById("Major").innerHTML + ", " + result[1]);
    				}
    				else{
    					$(".major").html(document.getElementById("Major").innerHTML + result[1]);
    				}
        		}
    			
        		else if (result[0] == "Minors" && result.length > 2){ //more than one word in the name
        			var moreThanOne = document.getElementById("Minor").innerHTML.split(" ");
        			if(moreThanOne.length > 1 && moreThanOne[1] != ""){
        				$(".minor").html(document.getElementById("Minor").innerHTML + ", " + result[1] + " " + result[2]);
    				}
    				else{
    					$(".minor").html(document.getElementById("Minor").innerHTML + result[1] + " " + result[2]);
    				}
        		}
        		else{ //only one word in the name
        			var moreThanOne = document.getElementById("Minor").innerHTML.split(" ");
        			if(moreThanOne.length > 1 && moreThanOne[1] != ""){
        				$(".minor").html(document.getElementById("Minor").innerHTML + ", " + result[1]);
    				}
    				else{
    					$(".minor").html(document.getElementById("Minor").innerHTML + result[1]);
    				}
        		}
        		
        		$(".error").html("<p></p>");
        		document.getElementById('major').value = "";
        		document.getElementById('minor').value = "";
        		
        	}
        });
        
        /**
         * updates the page with a major or minor removed
         */
        stompClient.subscribe('/client/remove', function (managing) {
        	result = managing.body;
        	majorMinor = managing.body.split(" ");
    	
    		if(majorMinor[0] == "Majors:"){
    			document.getElementById('Major').innerHTML = result;
    		}
    		else if(majorMinor[0] == "Minors:"){
    			document.getElementById('Minor').innerHTML = result;
    		}
    		else{
    			console.log("error");
    		}
        });
        
        /**
         * performs the auto-complete functionality 
         */
        stompClient.subscribe('/client/complete', function (managing) {
        	var which = managing.body.split(", ");
        	var list = which.slice(1, which.lenth);
        	console.log(list);
        	if(which[0] == "Majors"){
        		$( "#major" ).autocomplete({
        			source: list
        		});
        	}
        	else if(which[0] == "Minors"){
        		$( "#minor" ).autocomplete({
        			source: list
        		});
        	}
        	else{
        		console.log("error on autocomplete")
        	}

        	
        });
        
        stompClient.subscribe('/client/generate', function (managing) {
        	//TODO
        	
        	var schedule = JSON.parse(managing.body);
        	var courses = new Array();
        	var credits = new Array();
        	localStorage.setItem("num_semesters", schedule.length);
        	for(var i = 0; i < schedule.length; i++){
        		courses.push(new Array());
        		credits.push(new Array());
        		for(var k = 0; k < schedule[i].length; k++){
        			courses[i][k] = schedule[i][k].CourseID;
        			if(credits[i][k] == 'R' || credits[i][k] == 'arr')
        				credits[i][k] = schedule[i][k].Credits;
        			else
        				credits[i][k] = parseInt(schedule[i][k].Credits);
        			
        		}
        	}
        	localStorage.setItem("courses", JSON.stringify(courses));
        	localStorage.setItem("credits", JSON.stringify(credits));
        	localStorage.setItem("schedule", managing.body);
        	localStorage.setItem("fromAuto", true);
        	if(document.URL.includes('localhost'))
        	    window.location.href = "http://localhost:8080/manage";
        	else
        	    window.location.href = "http://proj-309-ss-1.cs.iastate.edu:8080/manage";
        	  
        });
        
    });
}

/**
 * sends a major or minor to the server side
 * @param input
 * a major or a minor
 * @returns
 */
function adding(input){
	
	stompClient.send("/app/AutoAdd",{}, input);
}

/**
 * Removes either a major or minor
 * @param input
 * the major or minor string to be modified
 * @returns
 */
function removing(input){
	
	stompClient.send("/app/AutoRemove",{}, input);
}

/**
 * The current input from the user is sent to server side
 * @param input
 * current input of the user in a text box
 * @returns
 */
function autoComplete(input){
	
	stompClient.send("/app/AutoComplete",{}, input);
}

/**
 * The current input from the user is sent to server side
 * @param input
 * current input of the user in a text box
 * @returns
 */
function generating(input){
	
	stompClient.send("/app/Generate",{}, input);
}

/**
 * is ready to erform the fowllowing functions when the 
 * page is ready
 */
$(document).ready(function () {
	connect();
	
	/**
	 * gets a major to be sent to the server side for adding to a list
	 */
    $(".addMaj").click(function () {
    	var major = document.getElementById('major').value;
    	var message = "Majors " + major;
    	adding(message);
    });
    
    /**
     * gets a minor to be sent to the server side for adding to a list
     */
    $(".addMin").click(function () {
    	var minor = document.getElementById('minor').value;	
    	var message = "Minors " + minor;
    	adding(message);
    });
    
    /**
     * gets a major to be sent to server side for removing from a list
     */
    $(".removeMajor").click(function () {
    	var message = document.getElementById('Major').innerHTML;
    	console.log(message);
    	removing(message);
    });
    
    /**
     * gets a minor to be sent to server side for removing from a list
     */
    $(".removeMinor").click(function () {
    	var message = document.getElementById('Minor').innerHTML;
    	console.log(message);
    	removing(message);
    });
    
    /**
     * gets the current input in the majors box to be sent to the server
     */
    $("#major").on('input', function(){
        autoComplete("Majors " + $("#major")[0].value);
    });

    
    /**
     * gets the current input in the minors box to be sent to the server
     */
	$("#minor").on('input', function(){
        autoComplete("Minors " + $("#minor")[0].value);
	});
    
	$(".submit").click(function () {
		var programs = localStorage.getItem("user") + ": " + $("#Major").html() + ": " + $("#Minor").html();
    	temp = programs.split(": ");
    	programs = temp[0] + ", " + temp[2] +  ", " + temp[4];
		
    	if(temp[2] != ""){
    		localStorage.setItem("program", temp[2]);
    	}
		if(temp[2] == "" && temp[4] == ""){
			$(".error").html("must enter a minor or major");
		}
		else{
	    	console.log(programs);
	    	generating(programs);
		}
    	
    });
    
});
	

     