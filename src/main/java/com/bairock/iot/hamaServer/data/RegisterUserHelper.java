package com.bairock.iot.hamaServer.data;

/**
 * 注册用户错误对象
 */
public class RegisterUserHelper {

	private long id = 0;
	private String name = "";
	private String petName = "";
	private String password = "";
	private String ensurePassword = "";

	private boolean autoLogin;
	// 用户名错误
	private String userNameError = "";
	// 密码错误
	private String passwordError = "";

	//是否是编辑,编辑和注册处理方法不一样
	private boolean isEdit = false;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserNameError() {
		return userNameError;
	}

	public void setUserNameError(String userNameError) {
		this.userNameError = userNameError;
	}

	public String getPasswordError() {
		return passwordError;
	}

	public void setPasswordError(String passwordError) {
		this.passwordError = passwordError;
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

	/**
	 * 判断两次输入的密码是否一致
	 * 
	 * @return true如果两个密码一致
	 */
	public boolean passwordEnsure() {
		return password.equals(ensurePassword);
	}

	public boolean isAutoLogin() {
		return autoLogin;
	}

	public void setAutoLogin(boolean autoLogin) {
		this.autoLogin = autoLogin;
	}

	public boolean isEdit() {
		return isEdit;
	}

	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}
	
}
