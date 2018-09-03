package com.bairock.iot.hamaServer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.bairock.iot.hamaServer.communication.PadChannelBridgeHelper;
import com.bairock.iot.hamaServer.data.webData.WebCtrlDev;
import com.bairock.iot.hamaServer.data.webData.WebDevBeanRoot;
import com.bairock.iot.hamaServer.data.webData.WebDevGear;
import com.bairock.iot.hamaServer.data.webData.WebUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class DevWebSocketController {

	private Logger logger = LoggerFactory.getLogger(DevWebSocketController.class);
	
	@MessageMapping("/userInfo")
	public void userInfo(WebUserInfo userInfo) {
		logger.info(userInfo.getUserName() + ":" + userInfo.getDevGroupName());
	}
	
	/**
	 * 网页发出的控制命令
	 * 控制命令相当于档位切换命令, 不需要单独发送档位命令
	 * @param ctrlDev
	 */
	@MessageMapping("/ctrlDev")
	public void ctrlDev(WebDevBeanRoot<WebCtrlDev> beanRoot) {
		WebCtrlDev ctrlDev = beanRoot.getData();
		logger.info(ctrlDev.getLongCoding() + ":" + ctrlDev.getAction());
		
		//向pad发送档位信息
		WebDevGear webDevGear = new WebDevGear(ctrlDev.getLongCoding(), ctrlDev.getAction());
        ObjectMapper mapper = new ObjectMapper();
		try {
			String order;
			order = mapper.writeValueAsString(webDevGear);
			PadChannelBridgeHelper.getIns().sendOrderSynable(beanRoot.getUserName(), beanRoot.getDevGroupName(), order);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.info("ctrlDev" + e.getMessage());
		}
	}
	
}
