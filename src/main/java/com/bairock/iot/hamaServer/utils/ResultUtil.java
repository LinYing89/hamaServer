package com.bairock.iot.hamaServer.utils;

import com.bairock.iot.hamaServer.data.Result;

public class ResultUtil {

	public static Result<Object> success() {
		Result<Object> r = new Result<>();
		r.setCode(0);
		return r;
	}
	
	public static Result<Object> error(int code, String message) {
		Result<Object> r = new Result<>();
		r.setCode(code);
		r.setMsg(message);
		return r;
	}
}
