package com.bairock.iot.hamaServer.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.data.webData.WebDevValue;
import com.bairock.iot.hamaServer.service.DeviceBroadcastService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;

@Component
public class MyOnCurrentValueChangedListener implements OnCurrentValueChangedListener {

	@Autowired
	private DeviceBroadcastService deviceService;
	
	@Override
	public void onCurrentValueChanged(DevCollect dev, Float value) {
		Device superParent = dev.findSuperParent();
		String userName = superParent.getDevGroup().getUser().getName();
		String devGroupName = superParent.getDevGroup().getName();
		if (dev instanceof DevCollect) {
			//通知网页
			WebDevValue webDevValue = new WebDevValue(dev.getLongCoding(), dev.getCollectProperty().getCurrentValue());
			deviceService.broadcastValueChanged(userName, devGroupName, webDevValue);
		}
	}

}
