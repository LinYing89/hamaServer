package com.bairock.iot.hamaServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.data.Result;
import com.bairock.iot.hamaServer.enums.ResultEnum;
import com.bairock.iot.hamaServer.exception.UserException;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.hamaServer.utils.ResultUtil;
import com.bairock.iot.intelDev.user.User;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	/**
	 * 用户数据上传
	 * @param user
	 */
	public Result<Object> userUpload(User user) throws Exception{
		
		if(null == user) {
			throw new UserException(ResultEnum.USER_UPLOAD_NULL);
		}
		return ResultUtil.success();
	}
}
