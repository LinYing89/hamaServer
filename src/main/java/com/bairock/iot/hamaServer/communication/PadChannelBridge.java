package com.bairock.iot.hamaServer.communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bairock.iot.hamaServer.SpringUtil;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.OrderHelper;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devswitch.SubDev;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.LoginModel;
import com.bairock.iot.intelDev.order.OrderBase;
import com.bairock.iot.intelDev.order.OrderType;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;
import com.bairock.iot.intelDev.user.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private String userName = "";
	private String groupName = "";
	// 是否需要向pad同步, true为需要, 当pad端无设备连接时, 为true, 当pad端有设备连接时为false, pad通过协议设置
	public boolean synable = true;
//	private DevGroup devGroup;
	// 持有设备
	private List<Device> listDevice = new ArrayList<>();
	private String channelId;
	public String loginModel = null;
	// the channel have no response count,0 if have response
	private int noReponse;

	private OnPadConnectedListener onPadConnectedListener;
	private OnPadMsgListener onPadMsgListener;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public PadChannelBridge() {
		myOnCurrentValueChangedListener = SpringUtil.getBean(MyOnCurrentValueChangedListener.class);
		myOnStateChangedListener = SpringUtil.getBean(MyOnStateChangedListener.class);
		myOnGearChangedListener = SpringUtil.getBean(MyOnGearChangedListener.class);
		myOnCtrlModelChangedListener = SpringUtil.getBean(MyOnCtrlModelChangedListener.class);

		onPadMsgListener = new MyOnPadMsgListener();
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
		onPadMsgListener.onReceived(userName, groupName, msg);

		logger.info("channelReceived userName:" + userName + " groupName:" + groupName + " msg:" + msg);
		noReponse = 0;

		try {
			ObjectMapper om = new ObjectMapper();
			DeviceOrder orderBase = om.readValue(msg, DeviceOrder.class);
			if(orderBase.getOrderType() != OrderType.HEAD_USER_INFO && null == loginModel) {
				return;
			}
			
//			OrderBase ob = new OrderBase();
			DeviceOrder devOrder = null;
			switch (orderBase.getOrderType()) {
			case HEAD_USER_INFO:
				userName = orderBase.getUsername();
				groupName = orderBase.getDevGroupName();
				if (null != onPadConnectedListener) {
					onPadConnectedListener.onPadConnected(userName, groupName);
				}
				loginModel = orderBase.getData();
				if (null != loginModel) {
					if (loginModel.equals(LoginModel.LOCAL)) {
						// 本地登录, 如果已有本地登录, 踢掉本次连接
						boolean haved = PadChannelBridgeHelper.getIns().LocalLoginHaved(this, userName, groupName);
						if(haved) {
							sendLogout();
						}
					} else {
						// 远程登录, 推送服务器设备状态到客户端
						sendInitStateToPad();
					}
				}
				break;
			case GEAR:
				if (userName != null && groupName != null) {
					sendToOtherClient(msg);
					devOrder = (DeviceOrder) orderBase;
					Device dev = findDevByCoding(devOrder.getLongCoding());
					if (null == dev) {
						return;
					}
					dev.setGear(Gear.valueOf(orderBase.getData()));
				}
				break;
			case CTRL_DEV:
				devOrder = (DeviceOrder) orderBase;
				Device dev = findDevByCoding(devOrder.getLongCoding());
				if (dev.getCtrlModel() == CtrlModel.REMOTE) {
					DevChannelBridge db = MyDevChannelBridge.findDevChannelBridge(dev.getLongCoding(), userName,
							groupName);
					if (null == db) {
						return;
					}
					db.sendOrder(orderBase.getData(), dev);
				} else {
					sendToOtherClient(msg);
				}
				break;
			case STATE:
				if (null == loginModel || !loginModel.equals(LoginModel.LOCAL)) {
					return;
				}
				sendToOtherClient(msg);
				devOrder = (DeviceOrder) orderBase;
				dev = findDevByCoding(devOrder.getLongCoding());
				if (null == dev) {
					return;
				}
				Device devParent = dev.findSuperParent();

                if(!devParent.isNormal()){
                    devParent.setDevStateId(DevStateHelper.DS_ZHENG_CHANG);
                }
                
                devParent.setCtrlModel(CtrlModel.LOCAL);
				dev.setDevStateId(orderBase.getData());
				break;
			case VALUE:
				if (null == loginModel || !loginModel.equals(LoginModel.LOCAL)) {
					return;
				}
				sendToOtherClient(msg);
				devOrder = (DeviceOrder) orderBase;
				dev = findDevByCoding(devOrder.getLongCoding());
				if (null == dev || !(dev instanceof DevCollect)) {
					return;
				}
				dev.setDevStateId(DevStateHelper.DS_ZHENG_CHANG);
				dev.setCtrlModel(CtrlModel.LOCAL);
				((DevCollect) dev).getCollectProperty().setCurrentValue(Float.parseFloat(orderBase.getData()));
				break;
			case TO_REMOTE_CTRL_MODEL:
				devOrder = (DeviceOrder) orderBase;
				dev = findDevByCoding(devOrder.getLongCoding());
				if (null == dev) {
					DeviceOrder feedbackOrder = new DeviceOrder();
					feedbackOrder.setOrderType(OrderType.MESSAGE);
					feedbackOrder.setData("无设备, 请先上传设备");
					send(Util.orderBaseToString(feedbackOrder));
					return;
				}
				DeviceOrder feedbackOrder = new DeviceOrder();
				feedbackOrder.setOrderType(OrderType.TO_REMOTE_CTRL_MODEL);
				feedbackOrder.setData("OK");
				send(Util.orderBaseToString(feedbackOrder));
				break;
			case TO_LOCAL_CTRL_MODEL:
				devOrder = (DeviceOrder) orderBase;
				dev = findDevByCoding(devOrder.getLongCoding());
				if (null == dev) {
					DeviceOrder feedbackOrder2 = new DeviceOrder();
					feedbackOrder2.setOrderType(OrderType.MESSAGE);
					feedbackOrder2.setData("无设备, 请先上传设备");
					send(Util.orderBaseToString(feedbackOrder2));
					return;
				}
				DevChannelBridge b = MyDevChannelBridge.findDevChannelBridge(devOrder.getLongCoding(), this.userName,
						this.groupName);
				b.sendOrder(devOrder.getData());
				break;
			default:
				break;
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	public DevChannelBridge findDevChannelBridge(String devCoding) {
//		for (DevChannelBridge bridge : DevChannelBridgeHelper.getIns().getListDevChannelBridge()) {
//			MyDevChannelBridge db = (MyDevChannelBridge)bridge;
//			if(db.getDevice() != null && db.getUserName().equals(userName) && db.getGroupName().equals(groupName)){
//				if(db.findDeviceByLongCoding(devCoding, db.getDevice()) != null) {
//					return db;
//				}
//			}
//		}
//		return null;
//	}

	public boolean judgeMsgFormate(String msg) {
		boolean formatOk = false;
		int len = msg.length();
		if (len < 3 || (!msg.endsWith("#") && !(msg.substring(len - 3, len - 2)).equals(OrderHelper.SUFFIX))) {
			formatOk = false;
		} else {
			formatOk = true;
		}
		return formatOk;
	}

	public String getHeart() {
		String heart = "";
		ObjectMapper om = new ObjectMapper();
		OrderBase ob = new OrderBase();
		//
		if (userName == null || groupName == null || userName.isEmpty() || groupName.isEmpty()) {
			ob.setOrderType(OrderType.HEAD_USER_INFO);
		} else {
//			ob.setOrderType(OrderType.HEAD_NOT_SYN);
			ob.setOrderType(OrderType.HEAD_SYN);
		}
		try {
			heart = om.writeValueAsString(ob);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return heart;
	}

	public void sendHeart() {
		sendMessage(getHeart());
	}

	private Device findDevice(String coding, List<Device> listDevice) {
		Device dev = null;
		for (Device d : listDevice) {
			if (d.getLongCoding().equals(coding)) {
				dev = d;
				break;
			} else if (d instanceof DevHaveChild) {
				dev = findDevice(coding, ((DevHaveChild) d).getListDev());
				if (null != dev) {
					break;
				}
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

	private Device findDevByCoding(String coding) {
		Device dev = findDevice(coding, listDevice);
		if (null == dev) {
			User user = userRepository.findByName(userName);
			DevGroup group = user.findDevGroupByName(groupName);
			dev = findDevByCoding(coding, group);
		}
		return dev;
	}

	private Device findDevByCoding(String coding, DevGroup group) {
		Device dev = findDevice(coding, listDevice);
		if (null == dev) {
			Device d = group.findDeviceWithCoding(coding);
			if (null == d) {
				return null;
			}
			if (d.getParent() != null) {
				Device parent = d.findSuperParent();
				parent = deviceService.findById(parent.getId());
				// 只需将父设备添加到集合, 寻找子设备时会遍历父设备的子设备集合
				addDeviceToList(parent);
				setDeviceListener(parent);
			} else {
				// 重新获取缓存中的数据, 使系统中设备对象唯一
				d = deviceService.findById(d.getId());
				addDeviceToList(d);
				setDeviceListener(d);
			}
			// 重新获取缓存中的数据, 使系统中设备对象唯一
			dev = findDevice(coding, listDevice);

		}
		return dev;
	}

	private void sendInitStateToPad() {
		User user = userRepository.findByName(userName);
		if (null == user) {
			return;
		}
		DevGroup group = user.findDevGroupByName(groupName);
		if (null == group) {
			return;
		}
		for (Device d : group.getListDevice()) {
			// 从缓存中获取对象
			Device dev = findDevByCoding(d.getLongCoding(), group);
			sendInitStateToPad(dev);
		}
	}

	private void sendInitStateToPad(Device dev) {
		if (null != dev && dev.isNormal() && dev.isVisibility()) {
			// 从缓存中读取对象, 保存状态一致
			DeviceOrder devOrder = null;
			if (dev instanceof DevCollect) {
				devOrder = new DeviceOrder(OrderType.VALUE, dev.getId(), dev.getLongCoding(),
						String.valueOf(((DevCollect) dev).getCollectProperty().getCurrentValue()));
			} else {
				devOrder = new DeviceOrder(OrderType.STATE, dev.getId(), dev.getLongCoding(), dev.getDevStateId());
				if (dev instanceof IStateDev) {
					// 发送档位
					DeviceOrder devo = new DeviceOrder(OrderType.GEAR, dev.getId(), dev.getLongCoding(),
							dev.getGear().toString());
					String strOrder = Util.orderBaseToString(devo);
					sendMessageNotReponse(strOrder);
				}
			}
			if (null != devOrder) {
				String strOrder = Util.orderBaseToString(devOrder);
				sendMessageNotReponse(strOrder);
			}
			if (dev instanceof DevHaveChild) {
				for (Device d : ((DevHaveChild) dev).getListDev()) {
					sendInitStateToPad(d);
				}
			}
		}
	}

	private void setDeviceListener(Device device) {

		if (device.getStOnStateChangedListener().isEmpty()) {
			device.setDevStateId(DevStateHelper.DS_UNKNOW);
			device.addOnStateChangedListener(myOnStateChangedListener);
		}
		if (device.getStOnCtrlModelChanged().isEmpty()) {
			device.setCtrlModel(CtrlModel.LOCAL);
			device.addOnCtrlModelChangedListener(myOnCtrlModelChangedListener);
		}
		if (device instanceof DevCollect) {
			DevCollect dc = (DevCollect) device;
			dc.getCollectProperty().addOnCurrentValueChangedListener(myOnCurrentValueChangedListener);
//			dc.getCollectProperty().initTriggerListener();
		} else if (device instanceof SubDev) {
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

	public void sendLogout() {
		OrderBase ob = new OrderBase();
		ob.setOrderType(OrderType.LOGOUT);
		send(Util.orderBaseToString(ob));
	}

	public void sendToAllClient(String msg) {
		for (PadChannelBridge pcb : PadChannelBridgeHelper.getIns().getListPadChannelBridge(userName, groupName)) {
			pcb.sendMessageNotReponse(msg);
		}
	}

	public void sendToOtherClient(String msg) {
		for (PadChannelBridge pcb : PadChannelBridgeHelper.getIns().getListPadChannelBridge(userName, groupName)) {
			if (pcb != this) {
				pcb.sendMessageNotReponse(msg);
			}
		}
	}

	public void sendMessage(String msg) {
		logger.info(" sendMessage userName:" + userName + " groupName:" + groupName + " msg:" + msg);
		if (null == getChannel()) {
			return;
		}
		if (noReponse > 6) {
			channel.close();
			PadChannelBridgeHelper.getIns().removeBridge(this);
		} else {
			noReponse++;
			if (null != getChannel()) {
				send(msg);
//				getChannel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
			}
		}
	}

	public void sendMessageNotReponse(String msg) {
		logger.info("send userName:" + userName + " groupName:" + groupName + " msg:" + msg);
		send(msg);
//	    getChannel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
	}

	private void send(String msg) {
		msg = msg + System.getProperty("line.separator");
		getChannel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
		onPadMsgListener.onSend(userName, groupName, msg);
	}

	public void close() {
		// 非本地pad不做处理
		if (null != loginModel && loginModel.equals(LoginModel.LOCAL)) {
			for (Device dev : listDevice) {
				if (dev.findSuperParent().getCtrlModel() == CtrlModel.LOCAL) {
					dev.setDevStateId(DevStateHelper.DS_YI_CHANG);
				}
			}
		}
	}

	public interface OnPadConnectedListener {
		void onPadConnected(String userName, String groupName);
	}

	public interface OnPadMsgListener {
		void onSend(String userName, String groupName, String msg);

		void onReceived(String userName, String groupName, String msg);
	}
}
