package com.bairock.iot.hamaServer.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.hamaServer.test.DeviceMsg;
import com.bairock.iot.hamaServer.test.DeviceMsgTestService;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.MessageAnalysiser;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.OrderHelper;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devswitch.SubDev;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

public class MyDevChannelBridge extends DevChannelBridge {

	private UserRepository userRepository;
	private DeviceService deviceService;

	private String userName = "";
	private String groupName = "";

	// 缓存收到的数据
	StringBuilder sb = new StringBuilder();

	private Logger logger = LoggerFactory.getLogger(MyDevChannelBridge.class);
	
	private MyOnCurrentValueChangedListener myOnCurrentValueChangedListener;
	private MyOnStateChangedListener myOnStateChangedListener;
	private MyOnGearChangedListener myOnGearChangedListener;
	private MyOnCtrlModelChangedListener myOnCtrlModelChangedListener;
	
	private DeviceMsgTestService deviceMsgTestService;

	public MyDevChannelBridge() {
		userRepository = SpringUtil.getBean(UserRepository.class);
		deviceService = SpringUtil.getBean(DeviceService.class);
		
		myOnCurrentValueChangedListener = SpringUtil.getBean(MyOnCurrentValueChangedListener.class);
		myOnStateChangedListener = SpringUtil.getBean(MyOnStateChangedListener.class);
		myOnGearChangedListener = SpringUtil.getBean(MyOnGearChangedListener.class);
		myOnCtrlModelChangedListener = SpringUtil.getBean(MyOnCtrlModelChangedListener.class);
		
		deviceMsgTestService = SpringUtil.getBean(DeviceMsgTestService.class);
		
		setOnCommunicationListener(new OnCommunicationListener() {

			@Override
			public void onSend(DevChannelBridge bridge, String msg) {
//				String info = "1" + createUserInfo() + " -> msg:" + msg;
//				logger.info(info);
//				RemoteLogWebSocket.sendMessageToAll(info);
				DeviceMsg dm = new DeviceMsg("send", msg);
				deviceMsgTestService.broadcastDeviceMsg(dm);
			}

			@Override
			public void onReceived(DevChannelBridge bridge, String msg) {
//				String info = "2" + createUserInfo() + " <- msg:" + msg;
//				logger.info(info);
//				RemoteLogWebSocket.sendMessageToAll(info);
				DeviceMsg dm = new DeviceMsg("rec", msg);
				deviceMsgTestService.broadcastDeviceMsg(dm);
			}
		});
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public void channelReceived(String msg, User user) {
//		logger.info(msg);

		if (null != getOnCommunicationListener()) {
			getOnCommunicationListener().onReceived(this, msg);
		}
		sb.append(msg);
		if (judgeMsgFormate(sb.toString())) {
			analysisReceiveMessage(msg);
			sb.setLength(0);
		}
	}
	
	@Override
	public void close() {
		super.close();
		if(null != getDevice()) {
//			removeDeviceListener(getDevice());
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
				if(null == user) {
					logger.error("no username : " + userName);
					return;
				}
				DevGroup group = user.findDevGroupByName(groupName);
				if(null == group) {
					logger.error("no groupname : " + groupName);
					return;
				}
				Device dev = group.findDeviceWithCoding(coding);
				if (null == dev) {
					return;
				}
				if(dev.getParent() != null) {
					Device parent = dev.findSuperParent();
					parent = deviceService.findById(parent.getId());
					setDevice(parent);
					dev.setUsername(userName);
					dev.setDevGroupName(groupName);
					//重新获取缓存中的数据, 使系统中设备对象唯一
					dev = ((DevHaveChild)parent).findDevByCoding(coding);
				}else {
					//重新获取缓存中的数据, 使系统中设备对象唯一
					dev = deviceService.findById(dev.getId());
					setDevice(dev);
					dev.setUsername(userName);
					dev.setDevGroupName(groupName);
				}
				this.userName = userName;
				this.groupName = groupName;
				getDevice().setCtrlModel(CtrlModel.REMOTE);
				setDeviceListener(getDevice());
				
				sendOrder(dev.createInitOrder());
				setDeviceToZhangChang(getDevice());
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
	
	public Device findDeviceByLongCoding(String coding, Device device) {
		if(null == device) {
			return null;
		}
		Device dev = null;
		if(device.getLongCoding().equals(coding)) {
			return device;
		}else if(device instanceof DevHaveChild){
			for(Device dd : ((DevHaveChild) device).getListDev()) {
				dev = findDeviceByLongCoding(coding, dd);
				if(null != dev) {
					return dev;
				}
			}
		}
		return dev;
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
	
	private void setDeviceListener(Device device) {
//		device.setCtrlModel(CtrlModel.UNKNOW);
		if(device.getStOnStateChangedListener().isEmpty()) {
			device.setDevStateId(DevStateHelper.DS_YI_CHANG);
			device.addOnStateChangedListener(myOnStateChangedListener);
		}
		if(device.getStOnCtrlModelChanged().isEmpty()) {
			device.addOnCtrlModelChangedListener(myOnCtrlModelChangedListener);
		}
		device.setCtrlModel(CtrlModel.REMOTE);
		if(device instanceof DevCollect) {
			DevCollect dc = (DevCollect)device;
			dc.getCollectProperty().addOnCurrentValueChangedListener(myOnCurrentValueChangedListener);
//			dc.getCollectProperty().initTriggerListener();
		}else if(device instanceof SubDev) {
			device.addOnGearChangedListener(myOnGearChangedListener);
		}
		if (device instanceof DevHaveChild) {
			for (Device device1 : ((DevHaveChild) device).getListDev()) {
				setDeviceListener(device1);
			}
		}
	}
	
//	private void removeDeviceListener(Device device) {
//		device.removeOnStateChangedListener(myOnStateChangedListener);
//		device.removeOnCtrlModelChangedListener(myOnCtrlModelChangedListener);
//		if(device instanceof DevCollect) {
//			DevCollect dc = (DevCollect)device;
//			dc.getCollectProperty().removeOnCurrentValueChangedListener(myOnCurrentValueChangedListener);
////			dc.getCollectProperty().initTriggerListener();
//		}else if(device instanceof SubDev) {
//			device.removeOnGearChangedListener(myOnGearChangedListener);
//		}
//		if (device instanceof DevHaveChild) {
//			for (Device device1 : ((DevHaveChild) device).getListDev()) {
//				setDeviceListener(device1);
//			}
//		}
//	}

	public static MyDevChannelBridge findBridge(Device device, String username, String devGroupName) {
//		if(null == device || null == device.findSuperParent().getDevGroup() || null == device.findSuperParent().getDevGroup().getUser()) {
//			return null;
//		}
		Device rootDev = device.findSuperParent();
		String groupName = rootDev.getDevGroup().getName();
		String userName = rootDev.getDevGroup().getUser().getName();
		DevChannelBridge d = DevChannelBridgeHelper.getIns().getDevChannelBridge(rootDev.getCoding(), userName,
				groupName);
		if (null != d) {
			return (MyDevChannelBridge) d;
		}
		return null;
	}
	
	public static DevChannelBridge findDevChannelBridge(String devCoding, String username, String groupName) {
		for (DevChannelBridge bridge : DevChannelBridgeHelper.getIns().getListDevChannelBridge()) {
			MyDevChannelBridge db = (MyDevChannelBridge)bridge;
			if(db.getDevice() != null && db.getUserName().equals(username) && db.getGroupName().equals(groupName)){
				if(db.findDeviceByLongCoding(devCoding, db.getDevice()) != null) {
					return db;
				}
			}
		}
		return null;
	}

}
