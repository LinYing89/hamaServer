package com.bairock.iot.hamaServer.test;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * 设备收发信息监视
 * @author 44489
 *
 */
@Controller
public class DeviceMsgTestWSCtrler {

	public static String deviceMsgFilter;
	
	@MessageMapping("/deviceMsg_filter")
	public void deviceMsgTestFilter(String filter) {
		filter = filter.trim();
		deviceMsgFilter = filter;
	}
}
