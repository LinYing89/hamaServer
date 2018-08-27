package com.bairock.iot.hamaServer.data;

/**
 * 设备组登录后返回的信息
 * @author 44489
 *
 */
public class DevGroupLoginResult {

	private String devGroupPetName;
	private int padPort;
	private int devPort;
	
	public String getDevGroupPetName() {
		return devGroupPetName;
	}
	public void setDevGroupPetName(String devGroupPetName) {
		this.devGroupPetName = devGroupPetName;
	}
	public int getPadPort() {
		return padPort;
	}
	public void setPadPort(int padPort) {
		this.padPort = padPort;
	}
	public int getDevPort() {
		return devPort;
	}
	public void setDevPort(int devPort) {
		this.devPort = devPort;
	}
	
	

}