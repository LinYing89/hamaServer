package com.bairock.iot.hamaServer.communication;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.data.webData.WebDevState;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.communication.RefreshCollectorValueHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devcollect.DevCollectClimateContainer;

/**
 * 设备状态改变事件监听器
 * @author 44489
 *
 */
public class MyOnStateChangedListener implements OnStateChangedListener {

	private DeviceService deviceService = SpringUtil.getBean(DeviceService.class);
	
	@Override
	public void onStateChanged(Device dev, String stateId) {
		Device superParent = dev.findSuperParent();
		String userName = superParent.getDevGroup().getUser().getName();
		String devGroupName = superParent.getDevGroup().getName();
		if (dev instanceof IStateDev || dev instanceof DevCollect) {
			//通知网页
			WebDevState webDevState = new WebDevState(dev.getLongCoding(), Integer.parseInt(dev.getDevState()));
			deviceService.broadcastStateChanged(userName, devGroupName, webDevState);
		}
	}

	@Override
	public void onNormalToAbnormal(Device dev) {
		//停止查询采集值
		if(dev instanceof DevCollectClimateContainer){
            RefreshCollectorValueHelper.getIns().endRefresh(dev);
        }
	}

	@Override
	public void onAbnormalToNormal(Device dev) {
		//开始查询采集值
		if(dev instanceof DevCollectClimateContainer){
            RefreshCollectorValueHelper.getIns().RefreshDev(dev);
        }
	}

	@Override
	public void onNoResponse(Device dev) {}

}