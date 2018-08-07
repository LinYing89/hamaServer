package com.bairock.iot.hamaServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.hamaServer.data.Config;

public interface ConfigRepository extends JpaRepository<Config, Long> {
}