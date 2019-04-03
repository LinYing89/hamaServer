package com.bairock.iot.hamaServer.exception;

import com.bairock.iot.intelDev.enums.ResultEnum;

/**
 * 用户信息异常
 * @author 44489
 *
 */
public class UserException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5857241398679731199L;
	
	private int code;

	public UserException(ResultEnum resultEnum) {
		super(resultEnum.getMessage());
		this.code = resultEnum.getCode();
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
