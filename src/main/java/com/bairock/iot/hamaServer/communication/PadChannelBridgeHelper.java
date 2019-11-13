package com.bairock.iot.hamaServer.communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bairock.iot.intelDev.order.LoginModel;

public class PadChannelBridgeHelper {

	private static PadChannelBridgeHelper ins = new PadChannelBridgeHelper();

	private List<PadChannelBridge> listPadChannelBridge = Collections.synchronizedList(new ArrayList<>());

	private OnPadDisconnectedListener onPadDisconnectedListener;
	
	public static PadChannelBridgeHelper getIns() {
		return ins;
	}

	private PadChannelBridgeHelper() {
		//IntelDevHelper.executeThread(new PadHeartThread());
	}

	public List<PadChannelBridge> getListPadChannelBridge(String userName, String groupName) {
		List<PadChannelBridge> list = new ArrayList<>();
		for (PadChannelBridge db : listPadChannelBridge) {
			if (null != db.getUserName() && null != db.getGroupName() && db.getUserName().equals(userName)
					&& db.getGroupName().equals(groupName)) {
				list.add(db);
			}
		}
		return list;
	}
	
	/**
	 * 是否已有本地登录
	 * @param userName
	 * @param groupName
	 * @return, 如果已有本地登录, 返回true, 否则false
	 */
	public boolean LocalLoginHaved(String userName, String groupName) {
		List<PadChannelBridge> list = getListPadChannelBridge(userName, groupName);
		for(PadChannelBridge pb : list) {
			if(null != pb.loginModel && pb.loginModel.equals(LoginModel.LOCAL)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 是否已有本地登录
	 * @param nowPb 准备登录的连接, 查找非本次连接的其他本地登录
	 * @param userName
	 * @param groupName
	 * @return, 如果已有本地登录, 返回true, 否则false
	 */
	public boolean LocalLoginHaved(PadChannelBridge nowPb, String userName, String groupName) {
		List<PadChannelBridge> list = getListPadChannelBridge(userName, groupName);
		for(PadChannelBridge pb : list) {
			if(null != pb.loginModel && pb.loginModel.equals(LoginModel.LOCAL) && pb != nowPb) {
				return true;
			}
		}
		return false;
	}

	public void setOnPadDisconnectedListener(OnPadDisconnectedListener onPadDisconnectedListener) {
		this.onPadDisconnectedListener = onPadDisconnectedListener;
	}

	public void sendOrder(String userName, String groupName, String order) {
		List<PadChannelBridge> list = getListPadChannelBridge(userName, groupName);
		for (PadChannelBridge pcb : list) {
			pcb.sendMessageNotReponse(order);
		}
	}
	
	public void sendOrderToLocal(String userName, String groupName, String order) {
		List<PadChannelBridge> list = getListPadChannelBridge(userName, groupName);
		for (PadChannelBridge pcb : list) {
			if(pcb.loginModel.equals(LoginModel.LOCAL)) {
				pcb.sendMessageNotReponse(order);
			}
		}
	}
	
	/**
	 * 向pad发送命令, 并且pad必须为可同步, 即synable为true
	 * @param userName
	 * @param groupName
	 * @param order
	 */
	public void sendOrderSynable(String userName, String groupName, String order) {
		List<PadChannelBridge> list = getListPadChannelBridge(userName, groupName);
		for (PadChannelBridge pcb : list) {
			if(pcb.synable) {
				pcb.sendMessageNotReponse(order);
			}
		}
	}

	public void setChannelId(String channelId) {
		boolean result = false;
		List<PadChannelBridge> list = listPadChannelBridge;
		for (PadChannelBridge db : list) {
			if (null == db || db.getChannelId() == null) {
				listPadChannelBridge.remove(db);
				continue;
			}
			if (db.getChannelId().equals(channelId)) {
				result = true;
				break;
			}
		}
		if (!result) {
			addBridge(channelId);
		}
	}

	public void channelReceived(String channelId, String msg) {
		List<PadChannelBridge> list = listPadChannelBridge;
		for (PadChannelBridge db : list) {
			if (null == db || db.getChannelId() == null) {
				listPadChannelBridge.remove(db);
				continue;
			}
			if (db.getChannelId().equals(channelId)) {
				db.channelReceived(msg);
				break;
			}
		}
	}

	private void addBridge(String channelId) {
		PadChannelBridge db = new PadChannelBridge();
		db.setChannelId(channelId);
		db.setOnPadConnectedListener(new MyOnPadConnectedListener());
		db.sendUserInfoHeart();
		listPadChannelBridge.add(db);
	}

	public void removeBridge(PadChannelBridge db) {
		listPadChannelBridge.remove(db);
		db.close();
		if(null != onPadDisconnectedListener && null != db) {
			onPadDisconnectedListener.onPadDisconnected(db.getUserName(), db.getGroupName());
		}
	}

	public void channelUnRegistered(String channelId) {
		List<PadChannelBridge> list = new ArrayList<>(listPadChannelBridge);
		for (PadChannelBridge db : list) {
			if (null == db || db.getChannelId() == null) {
				listPadChannelBridge.remove(db);
				continue;
			}
			if (db.getChannelId().equals(channelId)) {
				removeBridge(db);
			}
		}
	}
	
	public List<PadChannelBridge> findMyPadChannelBridge(String userName, String groupName){
		List<PadChannelBridge> listPb = new ArrayList<>(listPadChannelBridge);
		List<PadChannelBridge> list = new ArrayList<>();
		for (PadChannelBridge db : listPb) {
			if(db.getUserName() != null && db.getGroupName() != null 
					&& db.getUserName().equals(userName) && db.getGroupName().equals(groupName)) {
				list.add(db);
			}
		}
		return list;
	}

	/**
	 * 
	 * @author LinQiang
	 *
	 */
	public class PadHeartThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					sleep(10000);
					// System.out.println("DevHeartThread begin");
					if (listPadChannelBridge.isEmpty()) {
						continue;
					}

					List<PadChannelBridge> list = new ArrayList<>(listPadChannelBridge);
					// System.out.println("PadChannelBridgeHelper " + list.size());
					for (PadChannelBridge db : list) {
						db.sendHeart();
					}
				} catch (InterruptedException e) {
					// e.printStackTrace();
					break;
				}
			}
		}

	}
	
	public interface OnPadDisconnectedListener {
		void onPadDisconnected(String userName, String groupName);
	}
}
