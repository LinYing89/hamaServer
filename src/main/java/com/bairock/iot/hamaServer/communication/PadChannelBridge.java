package com.bairock.iot.hamaServer.communication;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.OrderHelper;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devswitch.SubDev;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class PadChannelBridge {

	public static ChannelGroup channelGroup = new DefaultChannelGroup("client", GlobalEventExecutor.INSTANCE);

	private Channel channel;
	private UserRepository userRepository = SpringUtil.getBean(UserRepository.class);
	private DeviceService deviceService = SpringUtil.getBean(DeviceService.class);
	private MyOnCurrentValueChangedListener myOnCurrentValueChangedListener;
	private MyOnStateChangedListener myOnStateChangedListener;
	private MyOnGearChangedListener myOnGearChangedListener;
	private MyOnCtrlModelChangedListener myOnCtrlModelChangedListener;
	
	private String userName="";
	private String groupName="";
	//是否需要向pad同步, true为需要, 当pad端无设备连接时, 为true, 当pad端有设备连接时为false, pad通过协议设置
	public boolean synable = true;
//	private DevGroup devGroup;
	//持有设备
	private List<Device> listDevice = new ArrayList<>();
	private String channelId;
	// the channel have no response count,0 if have response
	private int noReponse;

	private StringBuilder sbReceived = new StringBuilder();
	
	private OnPadConnectedListener onPadConnectedListener;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public PadChannelBridge() {
		myOnCurrentValueChangedListener = SpringUtil.getBean(MyOnCurrentValueChangedListener.class);
		myOnStateChangedListener = SpringUtil.getBean(MyOnStateChangedListener.class);
		myOnGearChangedListener = SpringUtil.getBean(MyOnGearChangedListener.class);
		myOnCtrlModelChangedListener = SpringUtil.getBean(MyOnCtrlModelChangedListener.class);
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

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public List<Device> getListDevice() {
		return listDevice;
	}

	public void setListDevice(List<Device> listDevice) {
		this.listDevice = listDevice;
	}

	public void setOnPadConnectedListener(OnPadConnectedListener onPadConnectedListener) {
		this.onPadConnectedListener = onPadConnectedListener;
	}

	public void channelReceived(String msg) {
		logger.info(" userName:" + userName + " groupName:" + groupName + " msg:" + msg);
		
//		WebDevGear webDevState = new WebDevGear("B39999_0_1", Integer.parseInt(msg));
//		deviceService.broadcastGearChanged("test123", "1", webDevState);
		
		noReponse = 0;
		sbReceived.append(msg);
		//System.out.println(sbReceived.length() + "?");
		if(judgeMsgFormate(sbReceived.toString())){
			displayMsg(sbReceived.toString());
			sbReceived.setLength(0);
		}else if(sbReceived.length() >= 20000) {
			sbReceived.setLength(0);
		}
	}
	
	public boolean judgeMsgFormate(String msg) {
		boolean formatOk = false;
		int len = msg.length();
		if (len < 3 || 
				(!msg.endsWith("#") &&!(msg.substring(len - 3, len - 2)).equals(OrderHelper.SUFFIX))) {
			formatOk = false;
		} else {
			formatOk = true;
		}
		return formatOk;
	}

	public String getHeart() {
		String heart;
		//
		if (userName == null || groupName == null) {
			heart = OrderHelper.getOrderMsg("h2");
		} else {
			heart = OrderHelper.getOrderMsg("h3");
		}
		return heart;
	}
	
	public void sendHeart() {
		sendMessage(getHeart());
	}
	
	private void displayMsg(String msg) {
		//System.out.println("PadChannelBridge displayMsg " + msg);
		try {
			if (null == msg) {
				return;
			}
			String[] arryMsg = msg.split("\\$");
			for (String str : arryMsg) {
				if (!str.isEmpty()) {
					analysisMsg(str);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void analysisMsg(String msg) {
		if (msg.startsWith("UN")) {
			if (msg.length() <= 3) {
				return;
			}
			String userMsg = msg.substring(2, msg.indexOf("#"));
			String[] arryMsg = userMsg.split(":");
			if (arryMsg.length != 2) {
				return;
			}
			userName = arryMsg[0];
			groupName = arryMsg[1];
			if(null != onPadConnectedListener) {
				onPadConnectedListener.onPadConnected(userName, groupName);
			}
		} else if (msg.startsWith("S")) {
			int index = msg.indexOf(":");
			if (index < 0 || index + 3 > msg.length()) {
				return;
			}
			
			String type = msg.substring(index + 1, index + 2);
			if(type.equals("s")) {
				//同步命令 SB10001:s[0,1]
				String state = msg.substring(index + 2);
				if(state.equals("0")) {
					synable = false;
				}else {
					synable = true;
				}
			}
			//配置远程和本地, 废弃
		} else if (msg.startsWith("C")) {
			if (!msg.contains("#") || !msg.contains(":")) {
				return;
			}
			String cutMsg = msg.substring(1, msg.indexOf("#"));
			int index = cutMsg.indexOf(":") + 1;
			String coding = cutMsg.substring(0, index - 1);
			DevChannelBridgeHelper.getIns().sendDevOrder(coding, "$" + msg, this.userName, this.groupName, true);
		} else {
			// like IB30006:707#5C
			if (msg.startsWith("I")) {
				if (userName != null && groupName != null) {
					sendToOtherClient(msg);
					analysisIMsg(msg);
				}
			}
		}
	}
	
	private Device findDevice(String coding, List<Device> listDevice) {
		Device dev = null;
		for(Device d : listDevice) {
			if(d.getCoding().equals(coding)) {
				dev = d;
				break;
			}else if(d instanceof DevHaveChild){
				findDevice(coding, ((DevHaveChild) d).getListDev());
			}
		}
		return dev;
	}
	
	private void addDeviceToList(Device dev) {
		dev.setUsername(userName);
		dev.setDevGroupName(groupName);
		listDevice.add(dev);
//		if(dev instanceof DevHaveChild) {
//			for(Device d : ((DevHaveChild) dev).getListDev()) {
//				addDeviceToList(d);
//			}
//		}
	}

	private void analysisIMsg(String msg) {
		if (!msg.contains("#") || !msg.contains(":")) {
			return;
		}
		
		String cutMsg = msg.substring(1, msg.indexOf("#"));
		int index = cutMsg.indexOf(":");
		String coding = cutMsg.substring(0, index);
		Device dev = findDevice(coding, listDevice);
		if(null == dev) {
			User user = userRepository.findByName(userName);
			DevGroup group = user.findDevGroupByName(groupName);
			Device d = group.findDeviceWithCoding(coding);
			if(null == d) {
				return;
			}
			if(d.getParent() != null) {
				Device parent = d.findSuperParent();
				parent = deviceService.findById(parent.getId());
				//只需将父设备添加到集合, 寻找子设备时会遍历父设备的子设备集合
				addDeviceToList(parent);
				setDeviceListener(parent);
			}else {
				//重新获取缓存中的数据, 使系统中设备对象唯一
				d = deviceService.findById(d.getId());
				addDeviceToList(d);
				setDeviceListener(d);
			}
			//重新获取缓存中的数据, 使系统中设备对象唯一
			dev = findDevice(coding, listDevice);
			
		}
		String state = cutMsg.substring(index + 1);
		if (state.startsWith("b")) {
			// gear
			String stateHead = cutMsg.substring(index + 2);
			dev.setGear(Enum.valueOf(Gear.class, stateHead));
		}else if(state.startsWith("2")) {
			String s1 = state.substring(1,2);
			if(s1.equals(DevStateHelper.getIns().getDs(DevStateHelper.DS_YI_CHANG))) {
				dev.setDevStateId(DevStateHelper.DS_YI_CHANG);
			}
		}else {
			dev.setCtrlModel(CtrlModel.LOCAL);
			dev.handle(state);
		}
	}
	
	private void setDeviceListener(Device device) {
		device.setCtrlModel(CtrlModel.UNKNOW);
		device.setDevStateId(DevStateHelper.DS_YI_CHANG);
		if(device.getStOnStateChangedListener().isEmpty()) {
			device.addOnStateChangedListener(myOnStateChangedListener);
		}
		if(device.getStOnCtrlModelChanged().isEmpty()) {
			device.addOnCtrlModelChangedListener(myOnCtrlModelChangedListener);
		}
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
	
	public Channel getChannel() {
		if (null == channel) {
			if (null == channelId) {
				return null;
			}

			for (Channel c : channelGroup) {
				if (c.id().asShortText().equals(channelId)) {
					channel = c;
					return channel;
				}
			}
		}
		return channel;
	}

	public void sendToAllClient(String msg) {
		for (PadChannelBridge pcb : PadChannelBridgeHelper.getIns().getListPadChannelBridge(userName, groupName)) {
			pcb.sendMessage("$" + msg);
		}
	}

	public void sendToOtherClient(String msg) {
		for (PadChannelBridge pcb : PadChannelBridgeHelper.getIns().getListPadChannelBridge(userName, groupName)) {
			if (pcb != this) {
				pcb.sendMessageNotReponse("$" + msg);
			}
		}
	}

	public void sendMessage(String msg) {
		logger.info(" userName:" + userName + " groupName:" + groupName + " msg:" + msg);
		if (noReponse > 2) {
			channel.close();
			PadChannelBridgeHelper.getIns().removeBridge(this);
		}else {
			noReponse++;
			getChannel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
		}
	}
	
	public void sendMessageNotReponse(String msg) {
		logger.info(" userName:" + userName + " groupName:" + groupName + " msg:" + msg);
	    getChannel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
	}
	
	public interface OnPadConnectedListener {
		void onPadConnected(String userName, String groupName);
	}
}
