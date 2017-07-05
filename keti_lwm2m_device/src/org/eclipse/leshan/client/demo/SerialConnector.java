package org.eclipse.leshan.client.demo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialConnector extends Thread{
	
    private static final String SERIAL_PORT = ConfigLoader.SerialPort;
    private static final int BAUD_RATE = ConfigLoader.BaudRate;
    
    private boolean isActive = false;
    private SerialPort sp = null;
    
    private boolean isSplitFrame = false;
    private StringBuffer msgBuilder = new StringBuffer();
    
    public SerialConnector() {
		// TODO Auto-generated constructor stub
    	
    	try{
	    	sp = new SerialPort(SERIAL_PORT);
			sp.openPort();
			sp.setParams(BAUD_RATE,
			        SerialPort.DATABITS_8, 
			        SerialPort.STOPBITS_1, 
			        SerialPort.PARITY_NONE);
	    	isActive = true;

    	}catch (Exception e) {
			// TODO: handle exception
		}
	}
    
    private Collection<SensorValueHandler> listeners;
    
    public void addReceiveListener(SensorValueHandler listener){
        if(listeners == null) {
            listeners = new HashSet<SensorValueHandler>();
        }
        listeners.add(listener);
    }
    
    public void removeReceiveListener(SensorValueHandler listener){
        if(listeners == null){
            return;
        }
        listeners.remove(listener);
    }
    
    private void notifyListeners(SensorValueRecieveEvent event){
        Iterator<SensorValueHandler> iter = listeners.iterator();
        while(iter.hasNext()){
        	SensorValueHandler listener = (SensorValueHandler) iter.next();
            listener.receiveSensingValue(event);
        }
    }
    
	public void begin(){
		isActive = true;
		this.start();
	}
	
	public void end(){
    	isActive = false;
    	
    	if(sp!=null&& sp.isOpened()){
    		try {
				sp.closePort();
				sp = null;
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
	
	public void send(String msg){
		if(sp != null && sp.isOpened()){
			try {
				sp.writeBytes(msg.getBytes());
				System.out.println("Send to device => " + msg);
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		 while(isActive){
             try {
            	 
            	 String ch = new String(sp.readBytes(1));
            	 
            	 if(isSplitFrame){
            		 msgBuilder.append(ch);
            	 }

            	 if(!isSplitFrame && ch.equals("{")){
            		 isSplitFrame = true;
            		 msgBuilder.append(ch);
            	 } else if(isSplitFrame && ch.equals("}")){
            		 
            		 System.out.println("Receive data <= " + msgBuilder.toString());
            		 
            		 JSONObject json = new JSONObject(msgBuilder.toString());
            		 
            		 double temp_value = json.getDouble("temp");
            		 double humi_value = json.getDouble("humi");
            		 
            		 SensorValueRecieveEvent event = new SensorValueRecieveEvent(this);
            		 event.setTempuratureValue(temp_value);
            		 event.setHumidityValue(humi_value);
            		 notifyListeners(event);
            		 
            		 msgBuilder.setLength(0);
            		 isSplitFrame = false;
            	 }
            	 
             } catch (SerialPortException ex) {
            	 ex.printStackTrace();
            	 
            	 msgBuilder.setLength(0);
             } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				 msgBuilder.setLength(0);
			}
         }
	}
}
