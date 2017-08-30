var stompClient = null;
/**
 * Connects this client (using Stomp and SockJS) to 
 * server's stomp socket endpoint
 * @author jacob
 */
function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
//        setConnected(true);
        console.log('Connected: ' + frame);
        
        stompClient.subscribe('/client/querying', function (managing) {
            if(managing.body!="fail")
            	$("#table_space").html(managing.body);
            else
            	$("#table_space").html("");
        });
        
    });
}
/**
 * Sends the message (a string) to the server
 * @param message
 * @author Jacob
 */
function send(message){
  stompClient.send("/app/manage",{},message);
}
$(document).ready(function () {
     connect();
    $( ".dropdown" ).change(function() {send(document.getElementById('drop1').value); });
});
