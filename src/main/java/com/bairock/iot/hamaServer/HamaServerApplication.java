package com.bairock.iot.hamaServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.bairock.iot.intelDev", "com.bairock.iot.hamaServer.data"})
public class HamaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HamaServerApplication.class, args);
	}
}
