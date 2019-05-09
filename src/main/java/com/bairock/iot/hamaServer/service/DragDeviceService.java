package com.bairock.iot.hamaServer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.iot.hamaServer.repository.DragDeviceRepository;
import com.bairock.iot.intelDev.data.DragDevice;

@Service
public class DragDeviceService {

    @Autowired
    private DragDeviceRepository dragDeviceRepository;
    
    public DragDevice findByDeviceId(String deviceId) {
        return dragDeviceRepository.findByDeviceId(deviceId);
    }
    
    public void insert(List<DragDevice> dragDevices) {
        for(DragDevice dd : dragDevices) {
            dragDeviceRepository.saveAndFlush(dd);
        }
    }
}
