package com.bairock.iot.hamaServer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.communication.MyDevChannelBridge;
import com.bairock.iot.hamaServer.communication.PadChannelBridge;
import com.bairock.iot.hamaServer.communication.PadChannelBridgeHelper;
import com.bairock.iot.hamaServer.data.Config;
import com.bairock.iot.hamaServer.repository.GroupRepository;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.data.DevGroupLoginResult;
import com.bairock.iot.intelDev.data.Result;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.enums.ResultEnum;
import com.bairock.iot.intelDev.linkage.LinkageHolder;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@Service
public class DevGroupService {

	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private CacheManager cacheManager;
	
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
		Result<DevGroupLoginResult> result = new Result<DevGroupLoginResult>();
		if(null == user) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}
		DevGroup group = user.findDevGroupByName(devGroupName);
		if(null == group) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}else if(!group.getPsd().equals(devGroupPsg)) {
			result.setCode(ResultEnum.ERR_PASSWORD.getCode());
			result.setMsg(ResultEnum.ERR_PASSWORD.getMessage());
			return result;
		}
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
		Result<DevGroup> result = new Result<>();
		if(null == user) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}
		DevGroup group = user.findDevGroupByName(devGroupName);
		if(null == group) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}
		
		result.setCode(ResultEnum.SUCCESS.getCode());
		result.setData(group);
		return result;
	}
	
	public Result<Object> groupUpload(User user){
		Result<Object> result = new Result<>();
		User userDb = userService.findByName(user.getName());
		if(null == userDb) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}
		DevGroup groupUpload = user.findDevGroupByName(user.getListDevGroup().get(0).getName());
		DevGroup groupDb = userDb.findDevGroupByName(user.getListDevGroup().get(0).getName());
		if(null == groupDb) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}
		
		List<Device> listOldDevice = new ArrayList<>(groupDb.getListDevice());
		
//		groupDb.getListDevice().clear();
		for(Device dev : listOldDevice) {
			groupDb.removeDevice(dev);
		}
		for(Device dev : groupUpload.getListDevice()) {
			groupDb.addDevice(dev);
		}
//		groupDb.getListDevice().addAll(groupUpload.getListDevice());
//		groupDb.getListLinkageHolder().clear();
		List<LinkageHolder> listOldLinkageHolder = new ArrayList<>(groupDb.getListLinkageHolder());
		for(LinkageHolder h : listOldLinkageHolder) {
			h.setDevGroup(null);
			groupDb.getListLinkageHolder().remove(h);
		}
		for(LinkageHolder h : groupUpload.getListLinkageHolder()) {
			h.setDevGroup(groupDb);
			groupDb.getListLinkageHolder().add(h);
		}
		groupDb.getListLinkageHolder().addAll(groupUpload.getListLinkageHolder());
		groupRepository.saveAndFlush(groupDb);
		
		//移除被删除设备的缓存
		for(Device dev : listOldDevice) {
			removeCacheDevice(dev);
		}
		
		for(DevChannelBridge bridge : DevChannelBridgeHelper.getIns().getListDevChannelBridge()) {
			MyDevChannelBridge myBridge = (MyDevChannelBridge)bridge;
			//找到已连接的设备, 并且用户信息一致的设备链接, 重新从缓存中获取设备
			if(null != myBridge.getUserName() && null != myBridge.getGroupName()) {
				if(myBridge.getUserName().equals(user.getName()) && myBridge.getGroupName().equals(groupUpload.getName())){
					Device oldDev = bridge.getDevice();
					if(null != oldDev) {
						Device dev = deviceService.findById(oldDev.getId());
						bridge.setDevice(dev);
					}
				}
			}
		}
		
		//找到所有已在pad链接中保存的设备对象, 重新从缓存中获取
		for(PadChannelBridge bridge : PadChannelBridgeHelper.getIns().getListPadChannelBridge(user.getName(), groupUpload.getName())) {
			List<Device> listDevice = new ArrayList<>();
			for(Device oldDev : bridge.getListDevice()) {
				Device dev = deviceService.findById(oldDev.getId());
				if(null != dev) {
					listDevice.add(dev);
				}
			}
			bridge.setListDevice(listDevice);
		}
		
		return result;
	}
	
	private void removeCacheDevice(Device device) {
		cacheManager.getCache("device").evict(device.getId());
		if(device instanceof DevHaveChild) {
			for(Device d : ((DevHaveChild) device).getListDev()) {
				removeCacheDevice(d);
			}
		}
	}
}
