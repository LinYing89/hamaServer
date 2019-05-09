package com.bairock.iot.hamaServer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.data.UserAuthority;
import com.bairock.iot.hamaServer.repository.UserAuthorityRepo;

@Component
public class MyCustomUserService implements UserDetailsService {

	@Autowired
	private UserAuthorityRepo userAuthorityRepo;
	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		com.bairock.iot.intelDev.user.User myUser = userService.findByUserid(userid);
		
		if(null != myUser) {
			UserAuthority ua = userAuthorityRepo.findByUserid(myUser.getUserid());
			if(null == ua) {
				ua = new UserAuthority();
				ua.setUserid(userid);
				ua.setAuthority("ROLE_USER");
			}
			List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
			GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(ua.getAuthority());
			// 1：此处将权限信息添加到 GrantedAuthority 对象中，在后面进行全权限验证时会使用GrantedAuthority 对象。
			grantedAuthorities.add(grantedAuthority);
			return new User(myUser.getUserid(), myUser.getPassword(), grantedAuthorities);
		}else {
			throw new UsernameNotFoundException("admin: " + userid + " do not exist!");
		}
		
	}

}