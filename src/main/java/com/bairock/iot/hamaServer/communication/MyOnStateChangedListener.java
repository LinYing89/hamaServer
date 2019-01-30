package com.bairock.iot.hamaServer.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.Util;
import com.bairock.iot.hamaServer.data.webData.WebDevState;
import com.bairock.iot.hamaServer.service.DeviceBroadcastService;
import com.bairock.iot.intelDev.communication.RefreshCollectorValueHelper;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devcollect.DevCollectClimateContainer;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;

/**
 * 设备状态改变事件监听器
 * 
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
			// 通知网页
			WebDevState webDevState = new WebDevState(dev.getLongCoding(), Integer.parseInt(dev.getDevState()));
			deviceService.broadcastStateChanged(superParent.getUsername(), superParent.getDevGroupName(), webDevState);
			
			//远程设备才发往本地, 本地设备状态在服务器收到之前已经到位了
			if (superParent.getCtrlModel() == CtrlModel.REMOTE) {
				// 发送到pad
				DeviceOrder devOrder = new DeviceOrder();
				devOrder.setOrderType(OrderType.STATE);
				devOrder.setDevId(dev.getId());
				devOrder.setLongCoding(dev.getLongCoding());
				devOrder.setData(stateId);
				String strOrder = Util.orderBaseToString(devOrder);
				PadChannelBridgeHelper.getIns().sendOrderSynable(superParent.getUsername(),
						superParent.getDevGroupName(), strOrder);
			}
		}
	}

	@Override
	public void onNormalToAbnormal(Device dev) {
		// 停止查询采集值
		if (dev instanceof DevCollectClimateContainer) {
			RefreshCollectorValueHelper.getIns().endRefresh(dev);
		}
	}

	@Override
	public void onAbnormalToNormal(Device dev) {
		// 开始查询采集值
		if (dev instanceof DevCollectClimateContainer) {
			RefreshCollectorValueHelper.getIns().RefreshDev(dev);
		}
	}

	@Override
	public void onNoResponse(Device dev) {
	}

}
