package com.bairock.iot.hamaServer.data.webData;

/**
 * 网页发送控制命令, 格式对象
 * @author 44489
 *
 */
public class WebCtrlDev {

	private String userName;
	private String devGroupName;
	
	private String longCoding;
	//动作, 1开, 0关, 2停
	private int action;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDevGroupName() {
		return devGroupName;
	}
	public void setDevGroupName(String devGroupName) {
		this.devGroupName = devGroupName;
	}
	public String getLongCoding() {
		return longCoding;
	}
	public void setLongCoding(String longCoding) {
		this.longCoding = longCoding;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	
	
}
