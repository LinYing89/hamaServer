package com.bairock.iot.hamaServer.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bairock.iot.hamaServer.service.DevGroupService;
import com.bairock.iot.hamaServer.service.UserService;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;
	@Autowired
	private DevGroupService devGroupService;
	
	@RequestMapping(value= {"/", "/loginSuccess"})
	public String loginSuccess(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
		String name = ((UserDetails)securityContext.getAuthentication().getPrincipal()).getUsername();
		if(name.equals("ggsb_public")) {
			name = "ggsb";
		}
		User user = userService.findByUserid(name);
		session.setAttribute("user", user);
		List<DevGroup> list = devGroupService.findByUserid(name);
		user.setListDevGroup(list);
		model.addAttribute("user", user);
		return "group/groupList";
	}
	
	@GetMapping(value= {"/login"})
	public String login() {
		return "login";
	}
	
//	@GetMapping(value = "/")
//    public String hello(Model model, RedirectAttributes rmodel, @CookieValue(value="userId",required=false) String userId){
//        if(null == userId || userId.isEmpty()){
//            return gotoLogin(model);
//        }
//        Optional<User> optionalUser = userRepository.findById(Long.parseLong(userId));
//        if(!optionalUser.isPresent()){
//            return gotoLogin(model);
//        }
//
//        User user = optionalUser.get();
//        
//        //为了使用缓存, 调用同一个方法获取的缓存对象才相同
//        user = userRepository.findByName(user.getName());
//        
//        //重定向
//        rmodel.addAttribute("userId", user.getId());
//        rmodel.addFlashAttribute("user", user);
//        return "redirect:/group/list/{userId}";
//    }

//    private String gotoLogin(Model model){
//        model.addAttribute("registerUserHelper", new RegisterUserHelper());
//        return "/login";
//    }
}
