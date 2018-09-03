package com.bairock.iot.hamaServer.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.OrderHelper;
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
	private String userName="";
	private String groupName="";
	//是否需要向pad同步, true为需要, 当pad端无设备连接时, 为true, 当pad端有设备连接时为false, pad通过协议设置
	public boolean synable = true;
	private DevGroup devGroup;
	private String channelId;
	// the channel have no response count,0 if have response
	private int noReponse;

	private StringBuilder sbReceived = new StringBuilder();
	
	private OnPadConnectedListener onPadConnectedListener;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
				}
				analysisIMsg(msg);
			}
		}
	}

	private void analysisIMsg(String msg) {
		if (!msg.contains("#") || !msg.contains(":")) {
			return;
		}
		
		String cutMsg = msg.substring(1, msg.indexOf("#"));
		int index = cutMsg.indexOf(":");
		if(this.devGroup == null) {
			User user = userRepository.findByName(userName);
			DevGroup group = user.findDevGroupByName(groupName);
			this.devGroup = group;
		}
		if (null != devGroup) {
			String coding = cutMsg.substring(0, index);
			Device dev = devGroup.findDeviceWithCoding(coding);
			if(null == dev) {
				return;
			}
			if(dev.getOnGearChanged() == null) {
				dev.setOnGearChanged(new MyOnGearChangedListener());
			}
			
			String state = cutMsg.substring(index + 1);
			if (state.startsWith("b")) {
				// gear
				String stateHead = cutMsg.substring(index + 2);
				dev.setGear(Enum.valueOf(Gear.class, stateHead));
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
