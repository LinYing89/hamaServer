package com.bairock.iot.hamaServer.enums;

public enum ResultEnum {

	UBKNOW_ERROR(-1, "未知错误"),
	SUCCESS(0, "成功"),
	USER_UPLOAD_NULL(1, "上传的用户信息对象为null"),
	USER_NAME_DB_NULL(2, "数据库中无对应用户名"),
	DEVGROUP_UPLOAD_NULL(3, "上传的组信息对象为null"),
	DEVGROUP_NULL(4, "设备组不存在"),
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
