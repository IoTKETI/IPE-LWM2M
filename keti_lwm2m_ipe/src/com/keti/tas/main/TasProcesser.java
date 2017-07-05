package com.keti.tas.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.json.JSONException;
import org.json.JSONObject;

import com.keti.tas.conf.ConfLoader;
import com.keti.tas.soft.MsgReceiveEvent;
import com.keti.tas.soft.MsgReceiveListener;
import com.keti.tas.soft.ReconnectHandler;
import com.keti.tas.soft.SimpleLwm2mServer;
import com.keti.tas.soft.ThymeClient;

public class TasProcesser implements ReconnectHandler
{
	public ThymeClient client;
	public ControllerRegistryThread registry;
	public SimpleLwm2mServer lwm2mServer;

	public void Start(){
		try{
		ConfLoader.loadFile("tas_conf.xml");
		
		client = new ThymeClient();
		registry = new ControllerRegistryThread();
		lwm2mServer = new SimpleLwm2mServer();
		
		client.addReceiveListener(new ThymeDataReceiver());
		client.setReconnectHandler(this);
		
		lwm2mServer.setMsgReceiver(new ThingDataReceiver());
		
		client.begin();
		registry.start();
		lwm2mServer.start();
		
		}catch(Exception exp){
			
		}
	}
	
	public void Stop(){
		//registry.end();
		client.end();
	}
	
	class ControllerRegistryThread extends Thread{
		
		private final Logger LOG = Logger.getLogger(ControllerRegistryThread.class.getName());
		
		private boolean isActived = false;
		
		public void end(){
			isActived = false;
		}
		
		@Override
		public void run() {
			isActived = true;
			while(isActived){
				try{
					if(ConfLoader.downloads != null && ConfLoader.downloads.size() > 0){
						for(int i = 0; i < ConfLoader.downloads.size(); i++){
							if(!ConfLoader.downloads.get(i).getHelloStatus()){
								
								JSONObject rootObj = new JSONObject();
								rootObj.put("ctname", ConfLoader.downloads.get(i).getContainerName());
								rootObj.put("con", "hello");
	
								client.send(rootObj.toString());
																
								LOG.log(Level.INFO, "controller [" + ConfLoader.downloads.get(i).getLwM2MPath() + "] send hello to thyme");
								
								Thread.sleep(1000);
							}
						}
					}
				}catch(JSONException exp){
					
				} catch (InterruptedException exp){
					
				} catch (Exception e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class ThymeDataReceiver implements MsgReceiveListener{
		
		private final Logger LOG = Logger.getLogger(ThymeDataReceiver.class.getName());
		
		@Override
		public void receiveMsgEvent(MsgReceiveEvent event) {
			try {
				String data = event.getMessage();
			
				if(!data.equals(null) && data.length() > 0){
					
					//LOG.log(Level.INFO, "Recevie a data [" + data + "] from thyme");

					JSONObject rootObj = new JSONObject(data);
					String ctname = rootObj.getString("ctname");
					String con = rootObj.getString("con");
					
					if(con.equals("hello")){
						for(int i = 0; i < ConfLoader.downloads.size(); i++){
							if(ConfLoader.downloads.get(i).getContainerName().equals(ctname)){
								ConfLoader.downloads.get(i).setHelloStatus(true);
								break;
							}
						}
					} else if(!con.equals("2001")&&!con.equals("<EOF>")) {
						for(int i = 0; i < ConfLoader.downloads.size(); i++){
							if(ConfLoader.downloads.get(i).getContainerName().equals(ctname)){
								if(ConfLoader.downloads.get(i).getHelloStatus()){

									lwm2mServer.controlDevice(ConfLoader.downloads.get(i).getLwM2MPath(), con);
									//thing.sendToThing(msg.getBytes());
									
									break;
								}
							}
						}	
					}
				}
			} catch (JSONException e) {

			}
		}
	}
	
	class ThingDataReceiver implements MsgReceiveListener{

		private final Logger LOG = Logger.getLogger(ThingDataReceiver.class.getName());
		
		@Override
		public void receiveMsgEvent(MsgReceiveEvent event) {
			try{
				String data = event.getMessage();
				
				if(data != null && data.length() > 0 && !data.equals("null")){
					
					LOG.log(Level.INFO, "Recevie a sensing data [" + data + "]");
					
					Document doc = DocumentHelper.parseText(data);
					
					String path = doc.selectSingleNode("msg/path").getText();
					String value = doc.selectSingleNode("msg/value").getText();
					value = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
					
					String[] ary = value.trim().split(", ");
					
					for(int i = 0; i < ary.length; i++){
						String f = ary[i].split("=")[0];
						String v = ary[i].split("=")[1];
						
						if(f.equals("value")){
							for(int j = 0; j < ConfLoader.uploads.size();j++){
								if(ConfLoader.uploads.get(j).getLwM2MPath().equals(path)){
									String cnt = ConfLoader.uploads.get(j).getContainerName();
									
									JSONObject rootObj = new JSONObject();
									rootObj.put("ctname", cnt);
									rootObj.put("con", v);
									
									client.send(rootObj.toString());
									
								}
							}
						}
					}
				}
			}catch(Exception exp){
				LOG.log(Level.SEVERE, "JSON parsing error!");
			}
		}
	}

	@Override
	public void reconnected() {
		// TODO Auto-generated method stub
		System.out.println("Try to reconnect.....");
		Stop();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Start();
	}
}