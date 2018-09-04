var stompClient;

var devGroupName = $('#span_groupName').text();
var userName = $('#a_userName').text();
console.info(userName + ":" + devGroupName);

$(document).ready(function() {

	// 初始化webSocket
	initWebSocket();

	$("tr").each(function(){
		var stateId = $(this).attr("data-state");
		console.info(stateId);
		switch(stateId){
		case "ds_g" :
			$(this).attr("class", "")
			break;
		case "ds_k" :
			$(this).attr("class", "table-success")
			break;
		case "ds_yc" :
			$(this).attr("class", "table-danger")
			break;
		}
	});
	
	
	// 开按钮
	$('.btn-on-dev').click(function() {
		ctrlClick(this.getAttribute("data-long-coding"), 1);
	});

	// 自动按钮
	$('.btn-auto-dev').click(function() {
		ctrlClick(this.getAttribute("data-long-coding"), 2);
	});

	// 关按钮
	$('.btn-off-dev').click(function() {
		ctrlClick(this.getAttribute("data-long-coding"), 0);
	});
});

function ctrlClick(longCoding, action) {
	stompClient.send("/app/ctrlDev", {}, JSON.stringify({
		'userName' : userName,
		'devGroupName' : devGroupName,
		'data' : {
			'longCoding' : longCoding,
			'action' : action
		}
	}));
}

// 初始化webSocket
function initWebSocket() {
	var socket = new SockJS('/hamaServer-websocket');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		console.info("stompClient connected:");
		stompClient.send("/app/userInfo", {}, JSON.stringify({
			'userName' : userName,
			'devGroupName' : devGroupName
		}));

		var userInfo = userName + "-" + devGroupName;
		var topicDevState = '/topic/' + userInfo + '/devState'
		stompClient.subscribe(topicDevState, handlerDevState);
		var topicDevGear = '/topic/' + userInfo + '/devGear'
		stompClient.subscribe(topicDevGear, handlerDevGear);
	});
}

function handlerDevState(message) {
	var devState = JSON.parse(message.body);
	var tr = $('#tr-' + devState.longCoding);
	var state = devState.state;
	switch (state) {
	case 0:
		tr.attr("class", "")
		//tr.removeClass("table-success");
		break;
	case 1:
		tr.attr("class", "table-success")
		//tr.addClass("table-success");
		break;
	case 4:
		tr.attr("class", "table-danger")
//		tr.removeClass("table-success");
//		tr.addClass("table-danger");
		break;
	}
}

function handlerDevGear(message) {
	var devGear = JSON.parse(message.body);
	var btnOn = $('#btnOn-' + devGear.longCoding);
	var btnAuto = $('#btnAuto-' + devGear.longCoding);
	var btnOff = $('#btnOff-' + devGear.longCoding);

	switch (devGear.gear) {
	case 0:
		btnOn.removeClass("active");
		btnAuto.removeClass("active");
		btnOff.addClass("active");
		break;
	case 1:
		btnOn.addClass("active");
		btnAuto.removeClass("active");
		btnOff.removeClass("active");
		break;
	case 2:
		btnOn.removeClass("active");
		btnAuto.addClass("active");
		btnOff.removeClass("active");
		break;
	}
}