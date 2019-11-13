package com.bairock.iot.hamaServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.bairock.iot.hamaServer.communication.MyDevChannelBridge;
import com.bairock.iot.hamaServer.communication.MyOnPadDisconnectedListener;
import com.bairock.iot.hamaServer.communication.PadChannelBridgeHelper;
import com.bairock.iot.hamaServer.communication.PadServer;
import com.bairock.iot.hamaServer.data.Config;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.DevServer;

@Component
public class MyApplicationRunner implements ApplicationRunner {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private Config config;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("TcpServerServicer begin");
        DevChannelBridgeHelper.DEV_CHANNELBRIDGE_NAME = MyDevChannelBridge.class.getName();
        DevServer.PORT = config.getDevicePort();
        logger.info("DevServer PORT: " + DevServer.PORT);
        try {
        	DevServer.getIns().run();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("DevServer start error " + e.getMessage());
		}
        
        PadChannelBridgeHelper.getIns().setOnPadDisconnectedListener(new MyOnPadDisconnectedListener());
        PadServer.PORT = config.getPadPort();
        logger.info("PadServer PORT: " + PadServer.PORT);
        PadServer padServer = new PadServer();
        try {
			padServer.run();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("PadServer start error " + e.getMessage());
		}
        logger.info("TcpServerServicer end");
        
//        DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();
//        DevChannelBridgeHelper.getIns().startSeekDeviceOnLineThread();
	}

}
