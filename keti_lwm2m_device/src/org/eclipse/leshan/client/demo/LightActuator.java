package org.eclipse.leshan.client.demo;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.json.JSONObject;

public class LightActuator extends BaseInstanceEnabler implements SensorValueHandler{

    private static final String UNIT_CELSIUS = "NAN";
    private static final int UNITS = 5701;
    private static final int COLOUR = 5706;
    private static final int CUMULATIVE_ACTIVE_POWER = 5805;
    private static final int POWER_FACTOR = 5820;
    private static final int POWER_ON_OFF = 5850;
    private static final int DIMMER = 5851;
    private static final int ON_TIME = 5852;
    
    private String colour = "greed";
    private float cumulativeActivePower = 0f;
    private float powerFactor = 0f;
    private boolean powerStatus = false;
    private int dimmer = 0;
    private int onTime = 0;
    
    private SerialConnector dev;
    
    public LightActuator(SerialConnector sc) {
    	this.dev = sc;
    	
    	if(this.dev != null){
    		this.dev.addReceiveListener(this);
    	}
    }
    
    @Override
    public synchronized ReadResponse read(int resourceid) {
        switch (resourceid) {
	        case UNITS:
	            return ReadResponse.success(resourceid, UNIT_CELSIUS);
	        case COLOUR:
	            return ReadResponse.success(resourceid, colour);
	        case CUMULATIVE_ACTIVE_POWER:
	            return ReadResponse.success(resourceid, cumulativeActivePower);
	        case POWER_FACTOR:
	            return ReadResponse.success(resourceid, powerFactor);
	        case POWER_ON_OFF:
	            return ReadResponse.success(resourceid, powerStatus);
	        case DIMMER:
	            return ReadResponse.success(resourceid, dimmer);
	        case ON_TIME:
	            return ReadResponse.success(resourceid, onTime);
	        default:
	            return super.read(resourceid);
        }
    }
    
    @Override
    public synchronized WriteResponse write(int resourceid, LwM2mResource value) {
    	 switch (resourceid) {
	    	 case COLOUR:
	    		 colour = (String) value.getValue();
	    		 return WriteResponse.success();
	    	 case POWER_ON_OFF:
    			 JSONObject root = new JSONObject();
    			 try {
					root.put("dev", "led");
					root.put("op", (boolean) value.getValue() ? "on":"off");
					
					this.dev.send(root.toString());
					
					powerStatus = (boolean) value.getValue();
    			 }catch (Exception e) {
					// TODO: handle exception
				 }
	    		 return WriteResponse.success();
	    	 case DIMMER:
	    		 dimmer = (int) value.getValue();
	    		 return WriteResponse.success();
	    	 case ON_TIME:
	    		 onTime = (int) value.getValue();
	    		 return WriteResponse.success();

	    	 default: return super.write(resourceid, value);
    	 }
    }
    

	@Override
	public void receiveSensingValue(SensorValueRecieveEvent event) {
		// TODO Auto-generated method stub
		
	} 
}
