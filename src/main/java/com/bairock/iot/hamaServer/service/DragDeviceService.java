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
        for (DragDevice dd : dragDevices) {
            DragDevice dragDevice = dragDeviceRepository.findByDeviceId(dd.getDeviceId());
            if (null == dragDevice) {
                dragDeviceRepository.saveAndFlush(dd);
            } else {
                dragDevice.setImageHeight(dd.getImageHeight());
                dragDevice.setImageWidth(dd.getImageWidth());
                dragDevice.setRotate(dd.getRotate());
                dragDevice.setImageName(dd.getImageName());
                dragDevice.setImageType(dd.getImageType());
                dragDevice.setLayoutx(dd.getLayoutx());
                dragDevice.setLayouty(dd.getLayouty());
                dragDeviceRepository.saveAndFlush(dragDevice);
            }
        }
    }
}
