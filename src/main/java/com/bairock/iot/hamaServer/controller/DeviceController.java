package com.bairock.iot.hamaServer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bairock.iot.hamaServer.service.DevGroupService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.DevGroup;

@Controller
@RequestMapping("/device")
public class DeviceController {

	@Autowired
	private DevGroupService devGroupService;
	
	@GetMapping("/page/{devGroupId}")
	public String findDevices(@PathVariable String devGroupId, Model model) {
		DevGroup group = devGroupService.findById(devGroupId);
		List<Device> listDevState = group.findListIStateDev(true);
		List<DevCollect> listDevValue = group.findListCollectDev(true);
		model.addAttribute("username", group.getUser().getName());
		model.addAttribute("devGroupName", group.getName());
		model.addAttribute("devGroupPetName", group.getPetName());
		model.addAttribute("listDevState", listDevState);
		model.addAttribute("listDevValue", listDevValue);
		return "device/devices";
	}
}
