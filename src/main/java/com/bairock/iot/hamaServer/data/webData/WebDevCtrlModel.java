package com.bairock.iot.hamaServer.data.webData;

public class WebDevCtrlModel extends WebDevBeanBase {

	// 0未知, 1本地, 2远程
	private int ctrlModel;

	public WebDevCtrlModel() {
	}

	public WebDevCtrlModel(int ctrlModel) {
		this.ctrlModel = ctrlModel;
	}

	public WebDevCtrlModel(String longCoding, int ctrlModel) {
		super(longCoding);
		this.ctrlModel = ctrlModel;
	}

	public int getCtrlModel() {
		return ctrlModel;
	}

	public void setCtrlModel(int ctrlModel) {
		this.ctrlModel = ctrlModel;
	}

}
