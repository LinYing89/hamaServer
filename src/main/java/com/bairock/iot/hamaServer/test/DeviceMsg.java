package com.bairock.iot.hamaServer.test;

public class DeviceMsg {

	//方向, send发送, rec接收
	private String direct;
	//报文
	private String msg;
	
	public DeviceMsg(String direct, String msg) {
		this.direct = direct;
		this.msg = msg;
	}
	
	public String getDirect() {
		return direct;
	}
	public void setDirect(String direct) {
		this.direct = direct;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
