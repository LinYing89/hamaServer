package com.bairock.iot.hamaServer.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bairock.iot.hamaServer.exception.UserException;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.hamaServer.utils.ResultUtil;
import com.bairock.iot.intelDev.data.Result;
import com.bairock.iot.intelDev.enums.ResultEnum;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

//	@Autowired
//	private UserService self;
	
	@Autowired
	private UserRepository userRepository;
	
	public User findById(long userId) {
		User user = userRepository.findById(userId).orElse(null);
//		if(null != user) {
//			return self.findByName(user.getName());
//		}
		return user;
	}
	
	@Cacheable(value="user", key="#userid")
	public User findByUserid(String userid) {
		RestTemplate rest = new RestTemplate();
//		String url = "http://localhost:8081/user/getUserInfo/" + userid;
		String url = "http://051801.cn:8081/user/getUserInfo/" + userid;
		String strRes = rest.getForObject(url, String.class);
		ObjectMapper om = new ObjectMapper();
		Result<User> result;
		try {
			result = om.readValue(strRes, new TypeReference<Result<User>>() {});
			if(result.getCode() == 0) {
				com.bairock.iot.intelDev.user.User myUser = result.getData();
				return myUser;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 用户数据上传
	 * @param user
	 */
	public Result<Object> userUpload(User user) throws Exception{
		
		if(null == user) {
			throw new UserException(ResultEnum.USER_UPLOAD_NULL);
		}
		if(user.getListDevGroup().size() == 0) {
			throw new UserException(ResultEnum.DEVGROUP_UPLOAD_NULL);
		}
		User userDb = userRepository.findByUserid(user.getUserid());
		if(null == userDb) {
			throw new UserException(ResultEnum.ERR_USERNAME);
		}
		DevGroup devGroup = user.getListDevGroup().get(0);
		DevGroup devGroupDb = userDb.findDevGroupByName(devGroup.getName());
		if(null == devGroupDb) {
			throw new UserException(ResultEnum.DEVGROUP_UPLOAD_NULL);
		}
		
		userDb.removeGroup(devGroupDb);
		devGroup.setId(devGroupDb.getId());
		userDb.addGroup(devGroup);
		userRepository.save(userDb); 
		return ResultUtil.success();
	}
	
	/**
	 * 下载用户信息
	 * @param name 下载的用户的用户名
	 * @return Result对象
	 * @throws Exception 下载异常信息
	 */
	public Result<User> userDownload(String name) throws Exception {
		User user = userRepository.findByUserid(name);
		Result<User> r = new Result<>();
		if(null == user) {
			throw new UserException(ResultEnum.ERR_USERNAME);
		}
		r.setCode(ResultEnum.SUCCESS.getCode());
		r.setData(user);
		return r;
	}
}
