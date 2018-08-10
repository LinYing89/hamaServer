package com.bairock.iot.hamaServer.enums;

public enum ResultEnum {

	UBKNOW_ERROR(-1, "未知错误"),
	SUCCESS(0, "成功"),
	USER_UPLOAD_NULL(1, "上传的用户信息对象为null"),
	DEVGROUP_UPLOAD_NULL(2, "上传的组信息对象为null")
	;
	
	private int code;
	private String message;
	
	private ResultEnum(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
}
