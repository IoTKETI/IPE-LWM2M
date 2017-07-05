/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keti.tas.soft;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.keti.tas.conf.ConfLoader;

/**
 *
 * @author ChenNan
 */
public class ThymeClient extends AbstractServer{
    
	private final static Logger LOG = Logger.getLogger(ThymeClient.class.getName());
	
    private final String serverIP = ConfLoader.ThymeHost;
    private final int serverPort = ConfLoader.ThymePort;
    private Socket client;
    private BufferedInputStream reader;
    private BufferedOutputStream  writer;
    
    private boolean isActived = false;
    
    private ReconnectHandler reconnectHandler = null;
    
    public void setReconnectHandler(ReconnectHandler reconnectHandler) {
		this.reconnectHandler = reconnectHandler;
	}
    
    public boolean isConnected(){
    	return isActived;
    }
    
    public void begin(){
    	isActived = true;
    	
    	try{
    		client = new Socket(serverIP, serverPort);
    		reader = new BufferedInputStream(client.getInputStream());
    		writer = new BufferedOutputStream(client.getOutputStream());
    	}catch(IOException exp){
    		LOG.log(Level.SEVERE, exp.getMessage());
    	}
    	this.start();
    }

    @Override
    public void run() {
    	while(isActived){
    		try{
	    		String respMsg = "";
	    		
	    		byte[] buf = new byte[4096];
	    		StringBuffer strbuf = new StringBuffer(4096);

	    		int read = 0;
	    		if((read = reader.read(buf)) > 0) {
	    			strbuf.append(new String(buf, 0, read));
	    			
		    		respMsg = new String(strbuf);
		    		
		    		String[] dataSet = respMsg.split("}");
		    		
		    		if(dataSet.length > 0){
			    		for(int i = 0; i < dataSet.length; i++){
			    			activeReceiveEvent(dataSet[i] + "}");
			    			//LOG.log(Level.INFO, "Receive a message [" + dataSet[i] + "}] from Thyme");
			    		}
		    		}
	    		}

    		}catch(IOException | NullPointerException exp){
    			isActived = false;
    			LOG.log(Level.SEVERE, "network is disconnected");
    			try {
    				if(client != null){
	    				if(!client.isClosed()){
	    					client.close();
	    				}
	    				while(client.isClosed()||!client.isConnected()){
	    					LOG.log(Level.SEVERE, "Try reconnect....!");
	    					client = new Socket(serverIP, serverPort);
	    					if(reconnectHandler != null){
	    						reconnectHandler.reconnected();
	    					}
	    			
		    				try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    			}
    				} else {
    					client = new Socket(serverIP, serverPort);
    				}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					LOG.log(Level.SEVERE, "Try reconnect failed!");
				}	
    		}
    	}
    }
    
    public void end() {
    	isActived = false;
    }
    
    public void send(String data){
    	if(client != null){
    		if(!client.isClosed()&&client.isConnected()){
    			try {
					writer.write(data.getBytes());
					writer.flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if(reconnectHandler != null){
						reconnectHandler.reconnected();
					}
				}
    			LOG.log(Level.INFO, "Client upload [" + data + "] to Thyme!");
    		} else {
    			try {
    				if(client != null){
        				if(!client.isClosed()){
        					client.close();
        				}
        				while(client.isClosed()||!client.isConnected()){
        					LOG.log(Level.SEVERE, "Try reconnect....!");
        					client = new Socket(serverIP, serverPort);
        					if(reconnectHandler != null){
        						reconnectHandler.reconnected();
        					}
        			
    	    				try {
    							Thread.sleep(2000);
    						} catch (InterruptedException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
    	    			}
    				} else {
    					client = new Socket(serverIP, serverPort);
    				}
    			} catch (IOException e1) {
    				// TODO Auto-generated catch block
    				LOG.log(Level.SEVERE, "Try reconnect failed!");
    			}	
    		}
    	} else {
    		try {
				if(client != null){
    				if(!client.isClosed()){
    					client.close();
    				}
    				while(client.isClosed()||!client.isConnected()){
    					LOG.log(Level.SEVERE, "Try reconnect....!");
    					client = new Socket(serverIP, serverPort);
    					if(reconnectHandler != null){
    						reconnectHandler.reconnected();
    					}
    			
	    				try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
				} else {
					client = new Socket(serverIP, serverPort);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				LOG.log(Level.SEVERE, "Try reconnect failed!");
			}	
    	}
    }
}
