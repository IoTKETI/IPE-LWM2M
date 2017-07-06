/**
 * Copyright (c) 2015, OCEAN
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by ChenNan in KETI on 2017-03-20.
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
