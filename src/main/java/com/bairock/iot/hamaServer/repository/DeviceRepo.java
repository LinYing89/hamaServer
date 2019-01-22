package com.bairock.iot.hamaServer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.device.Device;

public interface DeviceRepo extends JpaRepository<Device, String> {

	Device findByDevGroupIdAndId(String devGroupId, String id);
	
	List<Device> findByDevGroupId(String devGroupId);
}
