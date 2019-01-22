package com.bairock.iot.hamaServer.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bairock.iot.hamaServer.data.RegisterUserHelper;
import com.bairock.iot.hamaServer.data.Result;
import com.bairock.iot.hamaServer.service.UserService;
import com.bairock.iot.intelDev.user.User;

@Controller
@RequestMapping(value = "/user")
public class UserController {
	
	@Autowired
	private UserService userService;

    //打开注册页面
    @GetMapping("/page/register")
    public String registerForm() {
        return "register";
    }

    //提交注册信息
    @PostMapping("/register")
    public String registerSubmit(RedirectAttributes model, @ModelAttribute User user) {

    	userService.addUser(user);
        //重定向
        return "redirect:/loginSucces";
    }

    //打开登录页面
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("registerUserHelper", new RegisterUserHelper());
        return "login";
    }

    //提交登录信息
    @PostMapping("/login")
    public String loginCheck(HttpServletResponse httpServletResponse, RedirectAttributes model, @ModelAttribute RegisterUserHelper userHelper) {
        User userDb = userService.findByName(userHelper.getName());
        if(null == userDb){
            userHelper.setUserNameError("用户不存在");
            return "login";
        }
        //userDb.getDevGroups();
        if(!userDb.getPsd().equals(userHelper.getPassword())){
            userHelper.setPasswordError("密码错误");
            return "login";
        }
        if(userHelper.isAutoLogin()){
            Cookie cookie = new Cookie("userId", String.valueOf(userDb.getId()));
            cookie.setPath("/");//如果需要在跟目录获取,如欢迎页面,必须设置path为"/",否则只能在"/user"路径下获取到,其他路径获取不到
            cookie.setMaxAge(Integer.MAX_VALUE); //设置cookie的过期时间是10s
            httpServletResponse.addCookie(cookie);
        }

        //重定向
        model.addAttribute("userId", userDb.getId());

        model.addFlashAttribute("user", userDb);
        return "redirect:/group/list/{userId}";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse httpServletResponse, Model model){
        Cookie userCookie = new Cookie("userId", "");
        userCookie.setMaxAge(0);
        userCookie.setPath("/");
        httpServletResponse.addCookie(userCookie);
        model.addAttribute("registerUserHelper", new RegisterUserHelper());
        return "login";
    }
    
    /**
     * 用户数据上传
     * @param user
     * @return
     */
    @ResponseBody
    @PostMapping("/userUpload")
    public Result<Object> userUpload(@RequestBody User user) throws Exception{
    	return userService.userUpload(user);
    }
    
    /**
     * 用户数据下载
     * @param user
     * @return
     */
    @ResponseBody
    @GetMapping("/userDownload/{userName}")
    public Result<User> userDownload(@PathVariable String userName) throws Exception{
    	return userService.userDownload(userName);
    }
}
