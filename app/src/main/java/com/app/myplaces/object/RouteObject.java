package com.app.myplaces.object;

import java.util.ArrayList;

/**
 * 
 * LegObject.java
 * 
 * @Author DoBao
 * @Email baodt@hanet.vn
 * @Phone +84983028786
 * @Skype baopfiev_k50
 * @Date Feb 15, 2014
 * @Project WhereMyLocation
 * @Package com.ypyproductions.wheremylocation.object
 * @Copyright � 2014 Softwares And Network Solutions HANET Co., Ltd
 */
public class RouteObject extends ResultObject {

	private String distance;
	private String duration;
	private String endAddress;
	private String startAddress;
	private String overViewPolyline;
	private String summary;

	private ArrayList<StepObject> listStepObjects;

	public RouteObject() {
		super();
	}

	public RouteObject(String status) {
		super(status);
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getEndAddress() {
		return endAddress;
	}

	public void setEndAddress(String endAddress) {
		this.endAddress = endAddress;
	}

	public String getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	public ArrayList<StepObject> getListStepObjects() {
		return listStepObjects;
	}

	public void setListStepObjects(ArrayList<StepObject> listStepObjects) {
		this.listStepObjects = listStepObjects;
	}

	public String getOverViewPolyline() {
		return overViewPolyline;
	}

	public void setOverViewPolyline(String overViewPolyline) {
		this.overViewPolyline = overViewPolyline;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	
}
