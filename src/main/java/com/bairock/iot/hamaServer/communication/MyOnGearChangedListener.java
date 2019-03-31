package com.bairock.iot.hamaServer.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.Util;
import com.bairock.iot.hamaServer.data.webData.WebDevGear;
import com.bairock.iot.hamaServer.service.DeviceBroadcastService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;

/**
 * 档位改变监听器
 * 
 * @author 44489
 *
 */
@Component
public class MyOnGearChangedListener implements OnGearChangedListener {

	@Autowired
	private DeviceBroadcastService deviceService;

	@Override
	public void onGearChanged(Device dev, Gear gear, boolean touchDev) {
		// 设备异常不要发送档位了, 否则可能多个终端登录时, 造成状态不匹配终端之间循环发送
//		if (!dev.isNormal()) {
//			return;
//		}
		Device superParent = dev.findSuperParent();
		if (dev instanceof IStateDev) {
			// 通知网页, html操作先发往本地确认, 不会主动改变档位, 服务器档位的改变是因为收到本地的档位报文
			WebDevGear webDevGear = new WebDevGear(dev.getLongCoding(), dev.getGear().ordinal());
			deviceService.broadcastGearChanged(superParent.getUsername(), superParent.getDevGroupName(), webDevGear);

			// 触摸远程设备改变的档位要发往本地, 否则本地档位不变
			if (touchDev) {
				DeviceOrder devOrder = new DeviceOrder(OrderType.GEAR, dev.getId(), dev.getLongCoding(),
						gear.toString());
				String strOrder = Util.orderBaseToString(devOrder);
				PadChannelBridgeHelper.getIns().sendOrderSynable(superParent.getUsername(),
						superParent.getDevGroupName(), strOrder);
			}
		}
	}

}
