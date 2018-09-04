package com.bairock.iot.hamaServer.data.webData;

/**
 * 向网页发送采集设备值bean
 * @author 44489
 *
 */
public class WebDevValue extends WebDevBeanBase {

	private float value;
	
	public WebDevValue() {
	}

	public WebDevValue(String longCoding) {
		super(longCoding);
	}
	
	public WebDevValue(String longCoding, float value) {
		super(longCoding);
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

}
