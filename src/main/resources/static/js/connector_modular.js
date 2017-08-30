var stompClient = null;
/**
 * Connects this client (using Stomp and SockJS) to
 * server's stomp socket endpoint at the adress of to( a string)
 * wth the callback function callback_func.
 * @author jacob
 */
function connect(to,callback_func) {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
//        setConnected(true);
        console.log('Connected: ' + frame);

        stompClient.subscribe(to, callback_func);

    });
}
/**
 * Sends the message (a string) to the server
 * @param message
 * @author Jacob
 */
function send(to,message){
  stompClient.send(to,{},message);
}
