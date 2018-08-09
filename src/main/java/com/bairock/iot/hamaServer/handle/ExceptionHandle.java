package com.bairock.iot.hamaServer.handle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bairock.iot.hamaServer.data.Result;
import com.bairock.iot.hamaServer.exception.UserException;
import com.bairock.iot.hamaServer.utils.ResultUtil;

@ControllerAdvice
public class ExceptionHandle {

	private Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);
	
	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public Result<Object> handle(Exception e) {
		if(e instanceof UserException) {
			UserException ue = (UserException)e;
			return ResultUtil.error(ue.getCode(), ue.getMessage());
		}
		logger.error("系统错误:", e);
		return ResultUtil.error(-1, e.getMessage());
	}
}
