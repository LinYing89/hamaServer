package com.bairock.iot.hamaServer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.communication.PadChannelBridgeHelper;
import com.bairock.iot.hamaServer.data.webData.WebDevGear;
import com.bairock.iot.hamaServer.data.webData.WebDevState;
import com.bairock.iot.hamaServer.data.webData.WebDevValue;
import com.bairock.iot.hamaServer.repository.DeviceRepo;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeviceService {

	private SimpMessageSendingOperations messaging;
	
	@Autowired
	private DeviceRepo deviceRepo;
	@Autowired
	private CacheManager cacheManager;
	
	@Cacheable(value="device", key="#id")
	public Device findById(String id) {
		Device dev = deviceRepo.findById(id).orElse(null);
		if(null != dev) {
			setDeviceToCache(dev);
		}
		return dev;
	}
	
	//将设备和其子设备添加到缓存
	private void setDeviceToCache(Device dev) {
		if(null == dev) {
			return;
		}
		cacheManager.getCache("device").put(dev.getId(), dev);
		if(dev instanceof DevHaveChild) {
			for(Device d : ((DevHaveChild) dev).getListDev()) {
				setDeviceToCache(d);
			}
		}
	}

	public List<Device> findDeviceByDevGroupId(String devGroupId){
		return deviceRepo.findByDevGroupId(devGroupId);
	}
	
    @Autowired
    public DeviceService(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }
    
    /**
     * 向网页发送设备的状态bean
     * @param userName
     * @param devGroupName
     * @param webDevState
     */
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
    
    /**
     * 向采集设备发送设备的档位bean
     * @param userName
     * @param devGroupName
     * @param webDevGear
     */
    public void broadcastGearChanged(String userName, String devGroupName, WebDevGear webDevGear){
    	String topic = String.format("/topic/%s-%s/devGear", userName, devGroupName);
        messaging.convertAndSend(topic, webDevGear);
    }
    
    /**
     * 向网页发送采集设备的value bean
     * @param userName
     * @param devGroupName
     * @param webDevValue
     */
    public void broadcastValueChanged(String userName, String devGroupName, WebDevValue webDevValue){
    	String topic = String.format("/topic/%s-%s/devValue", userName, devGroupName);
        messaging.convertAndSend(topic, webDevValue);
    }
    
}
