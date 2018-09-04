package com.bairock.iot.hamaServer.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bairock.iot.hamaServer.data.RegisterUserHelper;
import com.bairock.iot.hamaServer.repository.UserRepository;
import com.bairock.iot.intelDev.user.User;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@GetMapping(value = "/")
    public String hello(Model model, RedirectAttributes rmodel, @CookieValue(value="userId",required=false) String userId){
        if(null == userId || userId.isEmpty()){
            return gotoLogin(model);
        }
        Optional<User> optionalUser = userRepository.findById(Long.parseLong(userId));
        if(!optionalUser.isPresent()){
            return gotoLogin(model);
        }

        User user = optionalUser.get();
        
        //为了使用缓存, 调用同一个方法获取的缓存对象才相同
        user = userRepository.findByName(user.getName());
        
        //重定向
        rmodel.addAttribute("userId", user.getId());
        rmodel.addFlashAttribute("user", user);
        return "redirect:/group/list/{userId}";
    }

    private String gotoLogin(Model model){
        model.addAttribute("registerUserHelper", new RegisterUserHelper());
        return "/login";
    }
}
