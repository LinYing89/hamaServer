var stompClient;

$(document).ready(function() {

	// 初始化webSocket
	initWebSocket();
	
	$('#btn-filter').click(function() {
		var filter = $("#input-filter").val();
		//不可发送空字符串
		if(filter == ""){
			filter = " ";
		}
		stompClient.send("/app/deviceMsg_filter", {}, filter);
	});
	$('#btn-clean').click(function() {
		$("#div-send").empty();
	});
});

//初始化webSocket
function initWebSocket() {
	var socket = new SockJS('/hamaServer-websocket');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		console.info("stompClient connected:");
		var topicDevState = '/topic/deviceMsg'
		stompClient.subscribe(topicDevState, handlerDeviceMsg);
	});
}

function handlerDeviceMsg(message) {
	var deviceMsg = JSON.parse(message.body);
	var text="";
	var divRoot = $("#div-send");
	var divText;
	if(deviceMsg.direct == "send"){
		text += "-> ";
		divText = $("<div class='text-dark'></div>");
	}else{
		text += "<- ";
		divText = $("<div class='text-danger'></div>");
	}
	var myDate = new Date();
	text += myDate.toLocaleString({ hour12: false });
	
	text += " " + deviceMsg.msg;
	divText.append(text);
//	divText = $("<div>" + text + "</div>");
	divRoot.append(divText);
}