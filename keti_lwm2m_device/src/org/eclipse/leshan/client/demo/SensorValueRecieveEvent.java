/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.eclipse.leshan.client.demo;

import java.util.EventObject;

/**
 *
 * @author ChenNan
 */
public class SensorValueRecieveEvent extends EventObject{

    private static final long serialVersionUID = 6496098798146410884L;

    private double temp;
    private double hum;
    
    public SensorValueRecieveEvent(Object o) {
        super(o);
    }
    
    public void setTempuratureValue(double value){
        this.temp = value;
    }
    
    public double getTempuratureValue(){
        return this.temp;
    }
    
    public void setHumidityValue(double value){
    	this.hum = value;
    }
    
    public double getHumidityValue(){
    	return this.hum;
    }
    
}
