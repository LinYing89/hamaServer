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
import com.bairock.iot.intelDev.data.DragConfig;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.data.Result;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
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
	private DragDeviceService dragDeviceService;
	@Autowired
	private DragConfigService dragConfigService;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private Config config;

	public DevGroup findById(String id) {
		return groupRepository.findById(id).orElse(null);
	}
	
	public List<DevGroup> findByUserid(String userid) {
		return groupRepository.findByUserid(userid);
	}
	
	public DevGroup findByNameAndUserid(String name, String userid) {
		return groupRepository.findByNameAndUserid(name, userid);
	}
	
//	@CachePut(value = "devGroup", key = "#result.id")
	public DevGroup addGroup(String userid, DevGroup group) {
		User user = userService.findByUserid(userid);
		if(null != user) {
			group.setId(UUID.randomUUID().toString());
			group.setUserid(userid);
			group.setUser(user);
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
	 * @param userid 账号
	 * @param devGroupName 组名
	 * @param devGroupPsg 组密码
	 */
	public Result<DevGroupLoginResult> devGroupLogin(String userid, String devGroupName, String devGroupPsg) throws Exception{
//		User user = userService.findByUserid(userid);
//		if(null == user) {
//			result.setCode(ResultEnum.ERR_USERNAME.getCode());
//			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
//			return result;
//		}
		Result<DevGroupLoginResult> result = new Result<DevGroupLoginResult>();
		DevGroup group = groupRepository.findByNameAndUserid(devGroupName, userid);
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
	 * @param userid 账号
	 * @param devGroupName 组名
	 * @return
	 * @throws Exception
	 */
	public Result<DevGroup> groupDownload(String userid, String devGroupName) throws Exception {
		DevGroup group = groupRepository.findByNameAndUserid(devGroupName, userid);
		Result<DevGroup> result = new Result<>();
		if(null == group) {
			result.setCode(ResultEnum.ERR_USERNAME.getCode());
			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
			return result;
		}
		
		result.setCode(ResultEnum.SUCCESS.getCode());
		result.setData(group);
		return result;
	}
	
	public Result<List<DragDevice>> dragDeviceDownload(String userid, String devGroupName) throws Exception {
        DevGroup group = groupRepository.findByNameAndUserid(devGroupName, userid);
        Result<List<DragDevice>> result = new Result<>();
        if(null == group) {
            result.setCode(ResultEnum.ERR_USERNAME.getCode());
            result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
            return result;
        }
        
        List<DragDevice> dragDevices = new ArrayList<>();
        
        List<Device> listDev = group.findListIStateDev(true);
        for (Device dev : listDev) {
            DragDevice dragDevice = dragDeviceService.findByDeviceId(dev.getId());
            if(null != dragDevice) {
                dragDevices.add(dragDevice);
            }
        }
        List<DevCollect> listDevCollector = group.findListCollectDev(true);
        for (Device dev : listDevCollector) {
            DragDevice dragDevice = dragDeviceService.findByDeviceId(dev.getId());
            if(null != dragDevice) {
                dragDevices.add(dragDevice);
            }
        }
        
        
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setData(dragDevices);
        return result;
    }
	
	public Result<DragConfig> dragConfigDownload(String userid, String devGroupName){
	    DevGroup group = groupRepository.findByNameAndUserid(devGroupName, userid);
        Result<DragConfig> result = new Result<>();
        if(null == group) {
            result.setCode(ResultEnum.ERR_USERNAME.getCode());
            result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
            return result;
        }
        
        DragConfig config = dragConfigService.findByDevGroupId(group.getId());
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setData(config);
        return result;
	}
	
	public Result<Object> groupUpload(DevGroup groupUpload){
		Result<Object> result = new Result<>();
//		User userDb = userService.findByUserid(user.getUserid());
//		if(null == userDb) {
//			result.setCode(ResultEnum.ERR_USERNAME.getCode());
//			result.setMsg(ResultEnum.ERR_USERNAME.getMessage());
//			return result;
//		}
//		DevGroup groupUpload = user.findDevGroupByName(user.getListDevGroup().get(0).getName());
		DevGroup groupDb = groupRepository.findByNameAndUserid(groupUpload.getName(), groupUpload.getUserid());
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
				if(myBridge.getUserName().equals(groupUpload.getUserid()) && myBridge.getGroupName().equals(groupUpload.getName())){
					Device oldDev = bridge.getDevice();
					if(null != oldDev) {
						Device dev = deviceService.findById(oldDev.getId());
						bridge.setDevice(dev);
					}
				}
			}
		}
		
		//找到所有已在pad链接中保存的设备对象, 重新从缓存中获取
		for(PadChannelBridge bridge : PadChannelBridgeHelper.getIns().getListPadChannelBridge(groupUpload.getUserid(), groupUpload.getName())) {
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
