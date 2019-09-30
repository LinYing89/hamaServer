package com.bairock.iot.hamaServer.data.webData;

/**
 * 向网页发送采集设备值bean
 * @author 44489
 *
 */
public class WebDevValue extends WebDevBeanBase {

	private String value;
	
	public WebDevValue() {
	}

	public WebDevValue(String longCoding) {
		super(longCoding);
	}
	
	public WebDevValue(String longCoding, String value) {
		super(longCoding);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
