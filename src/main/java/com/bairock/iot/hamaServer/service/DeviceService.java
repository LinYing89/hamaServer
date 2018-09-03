package com.bairock.iot.hamaServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.communication.PadChannelBridgeHelper;
import com.bairock.iot.hamaServer.data.webData.WebDevGear;
import com.bairock.iot.hamaServer.data.webData.WebDevState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeviceService {

	private SimpMessageSendingOperations messaging;

    @Autowired
    public DeviceService(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }
    
    //topic/userName-devGroupName/devState
    public void broadcastStateChanged(String userName, String devGroupName, WebDevState webDevState){
    	String topic = String.format("/topic/%s-%s/devState", userName, devGroupName);
        messaging.convertAndSend(topic, webDevState);
        
        //发往pad
        ObjectMapper mapper = new ObjectMapper();
		try {
			String order;
			order = mapper.writeValueAsString(webDevState);
			PadChannelBridgeHelper.getIns().sendOrderSynable(userName, devGroupName, order);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }
    
    public void broadcastGearChanged(String userName, String devGroupName, WebDevGear webDevGear){
    	String topic = String.format("/topic/%s-%s/devGear", userName, devGroupName);
        messaging.convertAndSend(topic, webDevGear);
    }
    
}
