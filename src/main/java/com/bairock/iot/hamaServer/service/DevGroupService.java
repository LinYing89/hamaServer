package com.bairock.iot.hamaServer.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.data.Config;
import com.bairock.iot.hamaServer.data.DevGroupLoginResult;
import com.bairock.iot.hamaServer.data.Result;
import com.bairock.iot.hamaServer.enums.ResultEnum;
import com.bairock.iot.hamaServer.exception.UserException;
import com.bairock.iot.hamaServer.repository.GroupRepository;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@Service
public class DevGroupService {

	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserService userService;
	
	@Autowired
	private Config config;

	public DevGroup findById(String id) {
		return groupRepository.findById(id).orElse(null);
	}
	
//	@CachePut(value = "devGroup", key = "#result.id")
	public DevGroup addGroup(long userId, DevGroup group) {
		User user = userService.findById(userId);
		if(null != user) {
			group.setId(UUID.randomUUID().toString());
			
//			DevGroup g = new DevGroup();
//			g.setId(UUID.randomUUID().toString());
			
			group.setUser(user);
//			user.addGroup(g);
//			userRepository.saveAndFlush(user);
			group = groupRepository.saveAndFlush(group);
		}
		return group;
	}

	/**
	 * 编辑组
	 * 
	 * @param id    组id
	 * @param model Model对象
	 * @return 编辑组页面
	 */
	public DevGroup editGroup(String id, DevGroup group) {
		DevGroup groupDb = groupRepository.findById(id).orElse(null);
		if(null != groupDb) {
			groupDb.setName(group.getName());
			groupDb.setPetName(group.getPetName());
			groupDb.setPsd(group.getPsd());
			groupRepository.saveAndFlush(groupDb);
		}
		
		return groupDb;
	}
	
	/**
	 * 删除组
	 * @param id 组id
	 * @param model Model对象
	 * @return 是否删除成功
	 */
//	@CacheEvict(value = "msgmanager", key = "#result.code")
	public DevGroup deleteGroup(String id) {
		DevGroup groupDb = groupRepository.findById(id).orElse(null);
		groupRepository.deleteById(id);
		return groupDb;
	}
	
	/**
	 * 客户端组登录
	 * @param userName 用户名
	 * @param devGroupName 组名
	 * @param devGroupPsg 组密码
	 */
	public Result<DevGroupLoginResult> devGroupLogin(String userName, String devGroupName, String devGroupPsg) throws Exception{
		User user = userService.findByName(userName);
		if(null == user) {
			throw new UserException(ResultEnum.USER_NAME_DB_NULL);
		}
		DevGroup group = groupRepository.findByNameAndPsdAndUserId(devGroupName, devGroupPsg, user.getId());
		if(null == group) {
			throw new UserException(ResultEnum.DEVGROUP_NULL);
		}
		Result<DevGroupLoginResult> result = new Result<DevGroupLoginResult>();
		result.setCode(0);
		DevGroupLoginResult r = new DevGroupLoginResult();
		r.setDevGroupId(group.getId());
		r.setDevGroupPetName(group.getPetName());
		r.setPadPort(config.getPadPort());
		r.setDevPort(config.getDevicePort());
		result.setData(r);
		return result;
	}
	
	/**
	 * 根据用户名和组名获取组
	 * @param userName 用户名
	 * @param devGroupName 组名
	 * @return
	 * @throws Exception
	 */
	public Result<DevGroup> groupDownload(String userName, String devGroupName) throws Exception {
		User user = userService.findByName(userName);
		if(null == user) {
			throw new UserException(ResultEnum.USER_NAME_DB_NULL);
		}
		DevGroup group = groupRepository.findByNameAndUserId(devGroupName, user.getId());
		if(null == group) {
			throw new UserException(ResultEnum.DEVGROUP_NULL);
		}
		Result<DevGroup> r = new Result<>();
		r.setCode(ResultEnum.SUCCESS.getCode());
		r.setData(group);
		return r;
	}
}
