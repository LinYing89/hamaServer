package com.bairock.iot.hamaServer.communication;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.data.webData.WebDevValue;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;

public class MyOnCurrentValueChangedListener implements OnCurrentValueChangedListener {

	private DeviceService deviceService = SpringUtil.getBean(DeviceService.class);
	
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
