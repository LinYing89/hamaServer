package com.bairock.iot.hamaServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.repository.DragConfigRepository;
import com.bairock.iot.intelDev.data.DragConfig;

@Service
public class DragConfigService {

    @Autowired
    private DragConfigRepository dragConfigRepository;
    
    public DragConfig findByDevGroupId(String devGroupId) {
        return dragConfigRepository.findByDevGroupId(devGroupId);
    }
    
    public void insert(DragConfig dragConfig) {
        dragConfigRepository.saveAndFlush(dragConfig);
    }
}
