package com.bairock.iot.hamaServer.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.data.webData.WebDevGear;
import com.bairock.iot.hamaServer.service.DeviceBroadcastService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;

/**
 * 档位改变监听器
 * @author 44489
 *
 */
@Component
public class MyOnGearChangedListener implements OnGearChangedListener {

	@Autowired
	private DeviceBroadcastService deviceService;
	
	@Override
	public void onGearChanged(Device dev, Gear gear) {
		Device superParent = dev.findSuperParent();
		if (dev instanceof IStateDev) {
			//通知网页
			WebDevGear webDevGear = new WebDevGear(dev.getLongCoding(), Integer.parseInt(dev.getDevState()));
			deviceService.broadcastGearChanged(superParent.getUsername(), superParent.getDevGroupName(), webDevGear);
		}
	}

}
