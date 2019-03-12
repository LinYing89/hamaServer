package com.bairock.iot.hamaServer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bairock.iot.hamaServer.data.Result;
import com.bairock.iot.hamaServer.enums.ResultEnum;
import com.bairock.iot.hamaServer.repository.DeviceImgRepo;
import com.bairock.iot.intelDev.data.DeviceImg;

@Controller
@RequestMapping("/deviceImg")
public class DeviceImgCtrler {

	@Autowired
	private DeviceImgRepo deviceImgRepo;
	
	@GetMapping("/list")
	private String getAllDeviceImg(Model model) {
		List<DeviceImg> list = deviceImgRepo.findAll();
		model.addAttribute("list", list);
		return "device/deviceImgList";
	}
	
	@ResponseBody
	@GetMapping("/checkVersionCode")
	private Result<List<DeviceImg>> checkVersionCode() {
		Result<List<DeviceImg>> result = new Result<>();
		List<DeviceImg> list = deviceImgRepo.findAll();
		result.setData(list);
		result.setCode(ResultEnum.SUCCESS.getCode());
		return result;
	}
}
