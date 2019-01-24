package com.bairock.iot.hamaServer.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.data.webData.WebDevState;
import com.bairock.iot.hamaServer.service.DeviceBroadcastService;
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
@Component
public class MyOnStateChangedListener implements OnStateChangedListener {

	@Autowired
	private DeviceBroadcastService deviceService;
	
	@Override
	public void onStateChanged(Device dev, String stateId) {
		Device superParent = dev.findSuperParent();
		if (dev instanceof IStateDev || dev instanceof DevCollect) {
			//通知网页
			WebDevState webDevState = new WebDevState(dev.getLongCoding(), Integer.parseInt(dev.getDevState()));
			deviceService.broadcastStateChanged(superParent.getUsername(), superParent.getDevGroupName(), webDevState);
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
