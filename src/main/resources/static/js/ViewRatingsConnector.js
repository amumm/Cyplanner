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
        stompClient.subscribe('/client/viewrates', function (managing) {
        	$("#table").html(managing.body);
        	
        });
    });
}
/**
 * sends message to server
 * @param message to be sent
 * 
 */
function send(message){
  stompClient.send("/app/viewratings",{},message);
}
/**
 * connects to server and sends input information on submit button
 */
$(document).ready(function () {
     connect();

     $("form").submit(function(){
    	 send(document.getElementById("viewcourse").value);
    	 return false;	
    		
     });
     $("button").click(function(){
    	 send(document.getElementById("viewcourse").value);
    	 event.preventDefault();
//         $("form").submit();
     });
     
});
