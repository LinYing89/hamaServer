package com.bairock.iot.hamaServer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.bairock.iot.hamaServer.Util;
import com.bairock.iot.hamaServer.communication.MyDevChannelBridge;
import com.bairock.iot.hamaServer.communication.PadChannelBridgeHelper;
import com.bairock.iot.hamaServer.data.webData.WebUserInfo;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;

/**
 * 组的设备页面websocket controller
 * 
 * @author 44489
 *
 */
@Controller
public class DevWebSocketController {

	@Autowired
	private DeviceService deviceService;
	private Logger logger = LoggerFactory.getLogger(DevWebSocketController.class);

	@MessageMapping("/userInfo")
	public void userInfo(WebUserInfo userInfo) {
		logger.info(userInfo.getUserName() + ":" + userInfo.getDevGroupName());
	}

	/**
	 * 向本地客户端发送刷新命令
	 * @param order
	 */
	@MessageMapping("/refresh")
    public void refresh(DeviceOrder order) {
	    sendCtrlOrderToPad(order);
    }
	
	/**
	 * 网页发出的控制命令 控制命令相当于档位切换命令, 不需要单独发送档位命令
	 * 
	 * @param ctrlDev
	 */
	@MessageMapping("/ctrlDev")
	public void ctrlDev(DeviceOrder order) {
		String devId = order.getDevId();
		Device dev = deviceService.findById(devId);
		if (dev == null) {
			return;
		}
		if(order.getOrderType() == OrderType.GEAR) {
			sendLocalOrder(order);
			return;
		}
		if (dev.getCtrlModel() == CtrlModel.LOCAL) {
			sendLocalOrder(order);
		} else if (dev.getCtrlModel() == CtrlModel.REMOTE) {
			sendRemoteOrder(dev, order);
		}else {
			sendLocalOrder(order);
			sendRemoteOrder(dev, order);
		}
	}
	
	private void sendLocalOrder(DeviceOrder order) {
//		if(order.getOrderType() == OrderType.CTRL_DEV) {
			sendCtrlOrderToPad(order);
//		}
//		sendGearToPad(order);
	}
	
	private void sendRemoteOrder(Device dev, DeviceOrder order) {
		if(order.getOrderType() != OrderType.CTRL_DEV) {
			return;
		}
		DevChannelBridge db = MyDevChannelBridge.findDevChannelBridge(dev.getLongCoding(), order.getUsername(), order.getDevGroupName());
		if(null == db) {
			return;
		}
		IStateDev subDev = (IStateDev)dev;
		String strOrder;
		if(order.getData().equals(DevStateHelper.DS_KAI)) {
			strOrder = subDev.getTurnOnOrder();
		}else {
			strOrder = subDev.getTurnOffOrder();
		}
		db.sendOrder(strOrder);
	}

	private void sendCtrlOrderToPad(DeviceOrder order) {
		String strOrder = Util.orderBaseToString(order);
		PadChannelBridgeHelper.getIns().sendOrderToLocal(order.getUsername(), order.getDevGroupName(), strOrder);
	}

}
