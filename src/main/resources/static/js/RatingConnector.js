
var stompClient = null;
/**
 * Connects to the server using stompClient
 * prints out Comment table
 */
function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
//        setConnected(true);
        console.log('Connected: ' + frame);
        
    });
}
/**
 * sends message to server
 * @param message to be sent
 * 
 */
function send(message){
  stompClient.send("/app/rate",{},message);//shipping
}
/**
 * concatenates values from form and sends it as one long message to server on submit
 */
$(document).ready(function sendString() {
     connect();
     $("form").submit(function(){
    	 event.preventDefault();
    		
    		
     });
     $("button").click(function(){
    	 event.preventDefault();
    	var course=document.getElementById("course").value;
 		var comment=document.getElementById("message").value;
 		var diff=document.getElementById("rate").value;
 		var work=document.getElementById("hours").value;
 		
 		document.getElementById("course").value="";
 		document.getElementById("message").value="";
 		document.getElementById("rate").value="";
 		document.getElementById("hours").value="";
 		
 		var string=course+'-'+comment+'-'+diff+'-'+work;
 		
 		send(string);
//         $("form").submit();
     });
     
});

