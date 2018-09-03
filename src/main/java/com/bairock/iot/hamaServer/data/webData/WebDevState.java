package com.bairock.iot.hamaServer.data.webData;

/**
 * 向网页发送设备状态bean
 * @author 44489
 *
 */
public class WebDevState extends WebDevBeanBase {

	//0关, 1开, 4异常
	private int state;
	
	public WebDevState() {}
	
	public WebDevState(int state) {
		super();
		this.state = state;
	}
	
	public WebDevState(String longCoding, int state) {
		super(longCoding);
		this.state = state;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
	
}
