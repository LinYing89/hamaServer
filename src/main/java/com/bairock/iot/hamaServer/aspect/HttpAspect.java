package com.bairock.iot.hamaServer.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HttpAspect {

	private Logger logger = LoggerFactory.getLogger(HttpAspect.class);
	
	@Before("execution(public * com.bairock.iot.hamaServer.controller.UserController.userUpload(..))")
	public void logUserUpload() {
		logger.info("upload");
	}
}
