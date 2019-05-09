package com.bairock.iot.hamaServer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.user.DevGroup;

public interface GroupRepository extends JpaRepository<DevGroup, String> {

	/**
	 * 根据用户ID查询所有设备组
	 * @param userId 用户账号
	 * @return 设备组集合
	 */
    List<DevGroup> findByUserid(String userid);
    
    /**
     * 根据组名,组密码和用户id查找设备组
     * @param name 组名
     * @param psd 组密码
     * @param userId 用户id
     * @return
     */
    DevGroup findByNameAndPsdAndUserid(String name, String psd, long userid);
    
    /**
     * 根据组名和用户id查找组
     * @param name
     * @param userId
     * @return
     */
    DevGroup findByNameAndUserid(String name, String userid);
}
