package com.bairock.iot.hamaServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * 根据用户名查找用户
	 * @param name 用户名
	 * @return 用户对象
	 */
	User findByUserid(String userid);
}
