package com.bairock.iot.hamaServer.data.webData;

/**
 * 网页档位通信bean
 * @author 44489
 *
 */
public class WebDevGear extends WebDevBeanBase {

	//0关, 1开, 2自动
	private int gear;
	
	public WebDevGear() {
	}

	public WebDevGear(int gear) {
		this.gear = gear;
	}

	public WebDevGear(String longCoding, int gear) {
		super(longCoding);
		this.gear = gear;
	}
	
	public int getGear() {
		return gear;
	}

	public void setGear(int gear) {
		this.gear = gear;
	}
	
	
}
