package com.bairock.iot.hamaServer.communication;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.data.webData.WebDevGear;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;

/**
 * 档位改变监听器
 * @author 44489
 *
 */
public class MyOnGearChangedListener implements OnGearChangedListener {

	private DeviceService deviceService = SpringUtil.getBean(DeviceService.class);
	
	@Override
	public void onGearChanged(Device dev, Gear gear) {
		Device superParent = dev.findSuperParent();
		String userName = superParent.getDevGroup().getUser().getName();
		String devGroupName = superParent.getDevGroup().getName();
		if (dev instanceof IStateDev) {
			//通知网页
			WebDevGear webDevGear = new WebDevGear(dev.getLongCoding(), Integer.parseInt(dev.getDevState()));
			deviceService.broadcastGearChanged(userName, devGroupName, webDevGear);
		}
	}

}
