package com.bairock.iot.hamaServer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bairock.iot.hamaServer.data.UserAuthority;
import com.bairock.iot.hamaServer.repository.UserAuthorityRepo;

@Controller
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	private UserAuthorityRepo userAuthorityRepo;
	
    //打开注册页面
    @GetMapping("/page/register")
    public String registerForm() {
        return "register";
    }

    //打开登录页面
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
    
    @GetMapping("/register/success")
    public String registerSuccess(@RequestParam String userid) {
    	UserAuthority ua = new UserAuthority();
		ua.setUserid(userid);
		ua.setAuthority("ROLE_USER");
		userAuthorityRepo.saveAndFlush(ua);
        return "login";
    }

//    //提交登录信息
//    @PostMapping("/login")
//    public String loginCheck(HttpServletResponse httpServletResponse, RedirectAttributes model, @ModelAttribute RegisterUserHelper userHelper) {
//        User userDb = userService.findByName(userHelper.getName());
//        if(null == userDb){
//            userHelper.setUserNameError("用户不存在");
//            return "login";
//        }
//        //userDb.getDevGroups();
//        if(!userDb.getPsd().equals(userHelper.getPassword())){
//            userHelper.setPasswordError("密码错误");
//            return "login";
//        }
//        if(userHelper.isAutoLogin()){
//            Cookie cookie = new Cookie("userId", String.valueOf(userDb.getId()));
//            cookie.setPath("/");//如果需要在跟目录获取,如欢迎页面,必须设置path为"/",否则只能在"/user"路径下获取到,其他路径获取不到
//            cookie.setMaxAge(Integer.MAX_VALUE); //设置cookie的过期时间是10s
//            httpServletResponse.addCookie(cookie);
//        }
//
//        //重定向
//        model.addAttribute("userId", userDb.getId());
//
//        model.addFlashAttribute("user", userDb);
//        return "redirect:/group/list/{userId}";
//    }

//    @PostMapping("/logout")
//    public String logout(HttpServletResponse httpServletResponse, Model model){
//        Cookie userCookie = new Cookie("userId", "");
//        userCookie.setMaxAge(0);
//        userCookie.setPath("/");
//        httpServletResponse.addCookie(userCookie);
//        model.addAttribute("registerUserHelper", new RegisterUserHelper());
//        return "login";
//    }
}
