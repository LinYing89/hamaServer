package com.bairock.iot.hamaServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.hamaServer.data.UserAuthority;

public interface UserAuthorityRepo extends JpaRepository<UserAuthority, Long> {

}
