package com.bairock.iot.hamaServer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Util {

	public static String encodePassword(String password) {
		BCryptPasswordEncoder bcry = new BCryptPasswordEncoder();
		String hashpw = bcry.encode(password);
		return hashpw;
	}
}
