package com.bairock.iot.hamaServer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.user.DevGroup;

public interface GroupRepository extends JpaRepository<DevGroup, Long> {

	/**
	 * 根据用户ID查询所有设备组
	 * @param userId 用户ID
	 * @return 设备组集合
	 */
    List<DevGroup> findByUserId(long userId);
    
    /**
     * 根据组名,组密码和用户id查找设备组
     * @param name 组名
     * @param psd 组密码
     * @param userId 用户id
     * @return
     */
    DevGroup findByNameAndPsdAndUserId(String name, String psd, long userId);
}
