package com.app.myplaces.object;

/**
 * 
 *  ResultObject.java	
 * @Author    DoBao
 * @Email     baodt@hanet.vn
 * @Phone     +84983028786
 * @Skype     baopfiev_k50
 * @Date      Dec 23, 2013  	
 * @Project   WhereMyLocation
 * @Package   com.ypyproductions.wheremylocation.object
 * @Copyright � 2013 Softwares And Network Solutions HANET Co., Ltd
 */
public class ResultObject {
	
	private String status="";
	
	
	public ResultObject() {
		super();
	}

	public ResultObject(String status) {
		super();
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
