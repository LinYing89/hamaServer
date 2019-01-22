package com.bairock.iot.hamaServer.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Config {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private int padPort = 20000;
    private int devicePort = 20001;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public int getPadPort() {
		return padPort;
	}
	public void setPadPort(int padPort) {
		this.padPort = padPort;
	}
	public int getDevicePort() {
        return devicePort;
    }
    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
    }

}