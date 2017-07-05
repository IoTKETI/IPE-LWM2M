package com.keti.tas.conf;

public class DeviceInfo {
	private String container_name;
	private String lwm2m_path;
	private boolean isHello;
	
	public DeviceInfo(){
		this.isHello = false;
	}
	
	public void setContainerName(String value){
		this.container_name = value;
	}
	
	public void setLwM2MPath(String value){
		this.lwm2m_path = value;
	}
	
	public void setHelloStatus(boolean value){
		this.isHello = value;
	}
	
	public String getContainerName(){
		return this.container_name;
	}
	
	public String getLwM2MPath(){
		return this.lwm2m_path;
	}
	
	public boolean getHelloStatus(){
		return this.isHello;
	}
}
