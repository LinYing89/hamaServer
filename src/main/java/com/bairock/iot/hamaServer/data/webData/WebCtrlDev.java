package com.bairock.iot.hamaServer.data.webData;

/**
 * 网页发送控制命令, 格式对象
 * @author 44489
 *
 */
public class WebCtrlDev {

	private String longCoding;
	private String stateId;
	
	public String getLongCoding() {
		return longCoding;
	}
	public void setLongCoding(String longCoding) {
		this.longCoding = longCoding;
	}
	public String getStateId() {
		return stateId;
	}
	public void setStateId(String stateId) {
		this.stateId = stateId;
	}
}
