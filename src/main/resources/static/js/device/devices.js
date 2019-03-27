var stompClient;

var liDevGroupInfo = $("#li-devgroup-info");
var devGroupName = liDevGroupInfo.data("devgroup-name");
var userName = liDevGroupInfo.data("username");
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
		case "ds_wz" :
			$(this).attr("class", "table-danger")
			break;
		}
	});
	
	
	// 开按钮
	$('.btn-on-dev').click(function() {
		ctrlClick($(this).data("dev-id"), $(this).data("long-coding"), 1);
	});

	// 自动按钮
	$('.btn-auto-dev').click(function() {
		ctrlClick($(this).data("dev-id"), $(this).data("long-coding"), 2);
	});

	// 关按钮
	$('.btn-off-dev').click(function() {
		ctrlClick($(this).data("dev-id"), $(this).data("long-coding"), 0);
	});
});

function ctrlClick(devId, longCoding, action) {
	var orderTypeCtrl;
	var dataCtrl;
	var dataGear;
	if(action == 2){
		dataGear = 'ZIDONG';
	}else{
		orderTypeCtrl = "CTRL_DEV";
		if(action == 0){
			dataCtrl = 'ds_g';
			dataGear = 'GUAN';
		}else{
			dataCtrl = "ds_k";
			dataGear = 'KAI';
		}
		stompClient.send("/app/ctrlDev", {}, JSON.stringify({
			'devId' : devId,
			'longCoding' : longCoding,
			'orderType' : orderTypeCtrl,
			'username' : userName,
			'devGroupName' : devGroupName,
			'data' : dataCtrl
		}));
	}
	
	stompClient.send("/app/ctrlDev", {}, JSON.stringify({
		'devId' : devId,
		'longCoding' : longCoding,
		'orderType' : "GEAR",
		'username' : userName,
		'devGroupName' : devGroupName,
		'data' : dataGear
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

		var userInfo = userName + ":" + devGroupName;
		var topicDevState = '/topic/' + userInfo + '/devState'
		stompClient.subscribe(topicDevState, handlerDevState);
		var topicDevGear = '/topic/' + userInfo + '/devGear'
		stompClient.subscribe(topicDevGear, handlerDevGear);
		var topicDevValue = '/topic/' + userInfo + '/devValue'
		stompClient.subscribe(topicDevValue, handlerDevValue);
		var topicDevCtrlModel = '/topic/' + userInfo + '/devCtrlModel'
		stompClient.subscribe(topicDevCtrlModel, handlerDevCtrlModel);
	});
}

function handlerDevState(message) {
	var devState = JSON.parse(message.body);
	var tr = $('#tr-' + devState.longCoding);
	var state = devState.state;
	switch (state) {
	case 0:
	case 3:
		tr.attr("class", "")
		//tr.removeClass("table-success");
		break;
	case 1:
		tr.attr("class", "table-success")
		//tr.addClass("table-success");
		break;
	case 4:
	case 7:
		tr.attr("class", "table-danger")
//		tr.removeClass("table-success");
//		tr.addClass("table-danger");
		break;
	}
}

function handlerDevGear(message) {
	var devGear = JSON.parse(message.body);
	var btnOn = $('#btn-on-' + devGear.longCoding);
	var btnAuto = $('#btn-auto-' + devGear.longCoding);
	var btnOff = $('#btn-off-' + devGear.longCoding);

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

function handlerDevValue(message) {
	var devValue = JSON.parse(message.body);
	var tdValue = $('#span-value-' + devValue.longCoding);
	tdValue.text(devValue.value);
}

function handlerDevCtrlModel(message) {
	var devValue = JSON.parse(message.body);
	var td = $('#span-ctrl-model-' + devValue.longCoding);
	if(devValue.ctrlModel == 1){
		td.text("本地");
	}else{
		td.text("远程");
	}
	
}