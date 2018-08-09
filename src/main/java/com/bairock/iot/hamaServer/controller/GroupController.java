package com.bairock.iot.hamaServer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.bairock.iot.hamaServer.data.RegisterUserHelper;
import com.bairock.iot.hamaServer.service.DevGroupService;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@Controller
@RequestMapping(value = "/group")
@SessionAttributes("user")
public class GroupController {

    @Autowired
    private DevGroupService devGroupService;
    
    //打开注册页面
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerGroupHelper", new RegisterUserHelper());
        return "group/groupRegister";
    }

    @GetMapping("/edit/{groupId}")
    public String editGroup(@PathVariable long groupId, Model model) {
    	return devGroupService.editGroup(groupId, model);
    }
    
    //注册页面提交
    @PostMapping("/register")
    public String registerSubmit(Model model, @ModelAttribute RegisterUserHelper groupHelper) {
    	if(devGroupService.registerSubmit(model, groupHelper)) {
    		return "group/groupList";
    	}else {
    		model.addAttribute("registerGroupHelper", groupHelper);
    		return "group/groupRegister";
    	}
    }

    @GetMapping("/delete/{groupId}")
    public String deleteGroup(@PathVariable long groupId, Model model) {
    	devGroupService.deleteGroup(groupId, model);
    	return "group/groupList";
    }
    
    //打开组列表页面
    @GetMapping("/list/{userId}")
    public String showGroupList(@PathVariable long userId, Model model) {
    	return devGroupService.showGroupList(userId, model);
    }

    //打开组页面
    @GetMapping("/{groupId}")
    public String showGroup(@PathVariable long groupId, Model model) {
        User user = (User) model.asMap().get("user");
        DevGroup group = user.findDevGroupById(groupId);
        model.addAttribute("devGroup", group);
        return "group/group";
    }
}
