package com.bairock.iot.hamaServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bairock.iot.hamaServer.data.Config;
import com.bairock.iot.hamaServer.repository.ConfigRepository;

@Configuration
public class AppConfig {

	@Bean
	@Autowired
	public Config createConfig(ConfigRepository configRepository){
        return configRepository.getOne(1L);
	}
}
