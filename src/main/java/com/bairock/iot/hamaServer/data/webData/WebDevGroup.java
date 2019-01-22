package com.bairock.iot.hamaServer.data.webData;

/**
 * 添加或编辑组时, 前端传入后端的数据
 * @author 44489
 *
 */
public class WebDevGroup {

	private String id = "";
	private String name = "";
	private String petName = "";
	private String password = "";
	private String ensurePassword = "";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPetName() {
		return petName;
	}
	public void setPetName(String petName) {
		this.petName = petName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEnsurePassword() {
		return ensurePassword;
	}
	public void setEnsurePassword(String ensurePassword) {
		this.ensurePassword = ensurePassword;
	}
	
}
