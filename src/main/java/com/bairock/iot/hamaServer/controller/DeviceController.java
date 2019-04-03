package com.bairock.iot.hamaServer.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bairock.iot.hamaServer.service.DevGroupService;
import com.bairock.iot.hamaServer.service.DeviceService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.DevGroup;

@Controller
@RequestMapping("/device")
public class DeviceController {

	@Autowired
	private DevGroupService devGroupService;
	@Autowired
	private DeviceService deviceService;
	
	@GetMapping("/page/{devGroupId}")
	public String findDevices(@PathVariable String devGroupId, Model model) {
		DevGroup group = devGroupService.findById(devGroupId);
		List<Device> listDevState = group.findListIStateDev(true);
		List<DevCollect> listDevValue = group.findListCollectDev(true);
		model.addAttribute("username", group.getUser().getName());
		
		//组昵称不为空显示组昵称, 否则显示组名
		String groupPetName = "";
		if(group.getPetName().isEmpty()) {
			groupPetName = group.getName();
		}else {
			groupPetName = group.getPetName();
		}
		model.addAttribute("devGroupName", group.getName());
		model.addAttribute("devGroupPetName", groupPetName);
		List<Device> listDevStateCache = new ArrayList<>();
		List<DevCollect> listDevValueCache = new ArrayList<>();
		for(Device dev : listDevState) {
			Device d = deviceService.findById(dev.getId());
			listDevStateCache.add(d);
		}
		for(Device dev : listDevValue) {
			DevCollect d = (DevCollect) deviceService.findById(dev.getId());
			listDevValueCache.add(d);
		}
		Collections.sort(listDevStateCache);
		Collections.sort(listDevValueCache);
		model.addAttribute("listDevState", listDevStateCache);
		model.addAttribute("listDevValue", listDevValueCache);
		return "device/devices";
	}
}
