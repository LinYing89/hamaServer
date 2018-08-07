package com.bairock.iot.hamaServer.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.bairock.iot.hamaServer.data.RegisterUserHelper;
import com.bairock.iot.hamaServer.repository.GroupRepository;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@Service
public class DevGroupService {

	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserRepository userRepository;

	/**
	 * 注册或编辑组页面提交
	 * 
	 * @param model       Model对象
	 * @param groupHelper 表单对象数据对象
	 * @return 注册或编辑成功返回true, 否则false
	 */
	public boolean registerSubmit(Model model, RegisterUserHelper groupHelper) {
		if (groupHelper.getName().isEmpty()) {
			groupHelper.setUserNameError("组名为空");
			return false;
		} else if (groupHelper.getName().length() > 16) {
			groupHelper.setUserNameError("用户名长度必须小于16");
			return false;
		}
		if (groupHelper.getPassword().isEmpty()) {
			groupHelper.setPasswordError("密码为空");
			return false;
		}
		if (!groupHelper.passwordEnsure()) {
			groupHelper.setPasswordError("两次输入的密码不一致");
			return false;
		}

		User user = (User) model.asMap().get("user");
		DevGroup devGroupDb = null;
		if (groupHelper.getId() != 0) {
			// 是编辑组
			devGroupDb = user.findDevGroupById(groupHelper.getId());
			devGroupDb.setName(groupHelper.getName());
			devGroupDb.setPetName(groupHelper.getPetName());
			devGroupDb.setPsd(groupHelper.getPassword());
			groupRepository.save(devGroupDb);
			return true;
		} else {
			// 注册组
			devGroupDb = user.findDevGroupByName(groupHelper.getName());
			if (null != devGroupDb) {
				groupHelper.setPasswordError("组名已存在");
				return false;
			}

			DevGroup group = new DevGroup();
			group.setName(groupHelper.getName());
			group.setPetName(groupHelper.getPetName());
			group.setPsd(groupHelper.getPassword());

			user.addGroup(group);
			groupRepository.save(group);
			return true;
		}
	}

	/**
	 * 编辑组
	 * 
	 * @param id    组id
	 * @param model Model对象
	 * @return 编辑组页面
	 */
	public String editGroup(long id, Model model) {
		User user = (User) model.asMap().get("user");
		DevGroup devGroupDb = user.findDevGroupById(id);
		RegisterUserHelper ru = new RegisterUserHelper();
		ru.setEdit(true);
		ru.setId(id);
		ru.setName(devGroupDb.getName());
		ru.setPetName(devGroupDb.getPetName());
		ru.setPassword(devGroupDb.getPsd());
		model.addAttribute("registerGroupHelper", ru);
		return "group/groupRegister";
	}

	public String showGroupList(long userId, Model model) {
		if (!model.containsAttribute("user")) {
			Optional<User> optionalUser = userRepository.findById(userId);
			optionalUser.ifPresent(user -> model.addAttribute("user", user));
		}
		return "group/groupList";
	}
	
	/**
	 * 删除组
	 * @param id 组id
	 * @param model Model对象
	 * @return 是否删除成功
	 */
	public boolean deleteGroup(long id, Model model) {
		User user = (User) model.asMap().get("user");
		DevGroup devGroupDb = user.findDevGroupById(id);
		if(null == devGroupDb) {
			return false;
		}
		user.removeGroup(devGroupDb);
		groupRepository.delete(devGroupDb);
		return true;
	}
}
