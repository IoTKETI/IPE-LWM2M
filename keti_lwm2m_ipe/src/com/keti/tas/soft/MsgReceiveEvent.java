/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keti.tas.soft;

import java.util.EventObject;

/**
 *
 * @author ChenNan
 */
public class MsgReceiveEvent extends EventObject{

    private static final long serialVersionUID = 6496098798146410884L;
    
    private String reveiveMessage = "";
    
    public MsgReceiveEvent(Object o, String msg) {
        super(o);
        this.reveiveMessage = msg;
    }
    
    public void setMessage(String msg){
        this.reveiveMessage = msg;
    }
    
    public String getMessage(){
        return this.reveiveMessage;
    }
}
