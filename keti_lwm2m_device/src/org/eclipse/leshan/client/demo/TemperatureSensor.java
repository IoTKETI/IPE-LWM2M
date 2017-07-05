package org.eclipse.leshan.client.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;

public class TemperatureSensor extends BaseInstanceEnabler implements SensorValueHandler {

    private static final String UNIT_CELSIUS = "℃";
    private static final int SENSOR_VALUE = 5700;
    private static final int UNITS = 5701;
    private static final int MAX_MEASURED_VALUE = 5602;
    private static final int MIN_MEASURED_VALUE = 5601;
    private static final int RESET_MIN_MAX_MEASURED_VALUES = 5605;
    
    private double currentTemp = 0;
    private double minMeasuredValue = -200;
    private double maxMeasuredValue = 100;
    
    private SerialConnector dev;

    public TemperatureSensor(SerialConnector sc) {
    	this.dev = sc;
    	
    	if(this.dev != null){
    		this.dev.addReceiveListener(this);
    	}
    } 

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
        case MIN_MEASURED_VALUE:
            return ReadResponse.success(resourceId, getTwoDigitValue(minMeasuredValue));
        case MAX_MEASURED_VALUE:
            return ReadResponse.success(resourceId, getTwoDigitValue(maxMeasuredValue));
        case SENSOR_VALUE:
            return ReadResponse.success(resourceId, getTwoDigitValue(currentTemp));
        case UNITS:
            return ReadResponse.success(resourceId, UNIT_CELSIUS);
        default:
            return super.read(resourceId);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(int resourceId, String params) {
        switch (resourceId) {
        case RESET_MIN_MAX_MEASURED_VALUES:
            return ExecuteResponse.success();
        default:
            return super.execute(resourceId, params);
        }
    }

    private double getTwoDigitValue(double value) {
        BigDecimal toBeTruncated = BigDecimal.valueOf(value);
        return toBeTruncated.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

	@Override
	public void receiveSensingValue(SensorValueRecieveEvent event) {
		// TODO Auto-generated method stub
		if(this.currentTemp != event.getTempuratureValue()){
			this.currentTemp = event.getTempuratureValue();
			fireResourcesChange(SENSOR_VALUE);
		};
	}
}
