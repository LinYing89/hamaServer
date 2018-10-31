package com.bairock.iot.hamaServer.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.MessageAnalysiser;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.OrderHelper;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

public class MyDevChannelBridge extends DevChannelBridge {

	private UserRepository userRepository;
	
	private String userName;
	private String groupName;
	
	//缓存收到的数据
	StringBuilder sb = new StringBuilder();
	
	private Logger logger = LoggerFactory.getLogger(MyDevChannelBridge.class);
	
	public MyDevChannelBridge() {
		userRepository = SpringUtil.getBean(UserRepository.class);
		
		setOnCommunicationListener(new OnCommunicationListener() {

			@Override
			public void onSend(DevChannelBridge bridge, String msg) {
				String info = "1" + createUserInfo() + " -> msg:" + msg;
				logger.info(info);
//				RemoteLogWebSocket.sendMessageToAll(info);
			}

			@Override
			public void onReceived(DevChannelBridge bridge, String msg) {
				String info = "2" + createUserInfo() + " <- msg:" + msg;
				logger.info(info);
//				RemoteLogWebSocket.sendMessageToAll(info);
			}
		});
	}
	
	private String createUserInfo() {
		String info = "u:";
		info += (null == userName ? "" : userName);
		info += "g:";
		info += (null == groupName ? "" : groupName);
		return info;
	}
	
	@Override
	public void channelReceived(String msg, User user) {
		 logger.info(msg);
		
		if (null != getOnCommunicationListener()) {
			getOnCommunicationListener().onReceived(this, msg);
		}
		sb.append(msg);
		if (judgeMsgFormate(sb.toString())) {
			analysisReceiveMessage(msg);
			sb.setLength(0);
		}
	}

	public boolean judgeMsgFormate(String msg) {
		boolean formatOk = false;
		int len = msg.length();
		if (len < 3 || !(msg.substring(len - 3, len - 2)).equals(OrderHelper.SUFFIX)) {
			formatOk = false;
		} else {
			formatOk = true;
		}
		return formatOk;
	}

	public void analysisReceiveMessage(String msg) {
		if (null == msg || !(msg.contains(OrderHelper.PREFIX)) || !(msg.contains(OrderHelper.SUFFIX))) {
			// MessageAnalysiser.listErrMsg.add(msg);
			return;
		}

		String[] arryMsg = msg.split("\\$");
		for (int i = 1; i < arryMsg.length; i++) {
			String data = arryMsg[i];
			analysisSingleMsg(data);
		}
	}
	
	public void analysisSingleMsg(String msg) {
		if (!msg.contains("#")) {
			return;
		}
		String[] codingState = MessageAnalysiser.findCodingState(msg);

		String coding = codingState[0];
		String state = codingState[1];

		if (null == getDevice()) {
			// 尝试获取用户名、组名
			String[] msgs = codingState[1].split(":");
			String userName = null;
			String groupName = null;
			for (String str : msgs) {
				if (str.startsWith("u")) {
					userName = str.substring(1);
				} else if (str.startsWith("g")) {
					groupName = str.substring(1);
				}
			}

			if (null != coding && null != userName && null != groupName) {
				User user = userRepository.findByName(userName);
				DevGroup group = user.findDevGroupByName(groupName);
				Device dev = group.findDeviceWithCoding(coding);
				if (null == dev) {
					return;
				}
				this.userName = userName;
				this.groupName = groupName;

				// dg.addDevice(dev1);
				// user1.addGroup(dg);
				dev.setCtrlModel(CtrlModel.UNKNOW);
				dev.setDevStateId(DevStateHelper.DS_YI_CHANG);
				setDevice(dev);
				//设置设备监听器
				DeviceService.setDeviceListener(dev);
				sendOrder(dev.createInitOrder());
				setDeviceToZhangChang(dev);
				if (null != state) {
					handleState(dev, state);
				}
			} 

		} else {
			if (null != coding) {
				if (!getDevice().getCoding().equals(coding) && getDevice() instanceof DevHaveChild) {
					Device childDev = ((DevHaveChild) getDevice()).findDevByCoding(coding);
					if (null != childDev) {
						handleState(childDev, state);
					}
				} else {
					handleState(getDevice(), state);
				}
			}
		}
	}

	private void setDeviceToZhangChang(Device dev) {
		dev.setDevStateId(DevStateHelper.DS_ZHENG_CHANG);
//		if (dev instanceof DevHaveChild) {
//			for (Device device : ((DevHaveChild) dev).getListDev()) {
//				setDeviceToZhangChang(device);
//			}
//		}
	}

	private void handleState(Device dev, String state) {
		if (null == dev || state == null) {
			return;
		}
		if (dev.getCtrlModel() != CtrlModel.REMOTE) {
			dev.setCtrlModel(CtrlModel.REMOTE);
		}
		dev.handle(state);
	}
	
	public static MyDevChannelBridge findBridge(Device device) {
//		if(null == device || null == device.findSuperParent().getDevGroup() || null == device.findSuperParent().getDevGroup().getUser()) {
//			return null;
//		}
		Device rootDev = device.findSuperParent();
		String groupName = rootDev.getDevGroup().getName();
		String userName = rootDev.getDevGroup().getUser().getName();
		DevChannelBridge d = DevChannelBridgeHelper.getIns().getDevChannelBridge(rootDev.getCoding(), userName, groupName);
		if(null != d) {
			return (MyDevChannelBridge)d;
		}
		return null;
	}
	
}
