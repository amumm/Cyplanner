var stompClient = null;


function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
//        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/client/Departments-query', function (message) {
            if(message.body!="fail")
            	$("#table_space").html(message.body);
            else
            	$("#table_space").html("");
        });
        stompClient.subscribe('/client/Departments-autocomplete', function (message) {
//        	console.log(message.body);
        	var ar=message.body.split(", ");
        	for(i=0;i<ar.length-1;i++){
        		console.log(ar[i]);
        	}
        	$("#dep_in").autocomplete({source:ar});
        });
    });
}
function send(message){
  stompClient.send("/app/Departments-query",{},message);
}

function autocomplete(message){
	stompClient.send("/app/Departments-autocomplete",{},message);
}

$(document).ready(function () {
     connect();
    $( "#dep_in" ).on('input',function() {autocomplete(document.getElementById('dep_in').value); });
    $( "#dep_in" ).change(function() {
    	send(document.getElementById('dep_in').value);
    });
});