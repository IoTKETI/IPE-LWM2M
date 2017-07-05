/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keti.tas.soft;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author ChenNan
 */
public abstract class AbstractServer extends Thread {
    private Collection<MsgReceiveListener> listeners;
  
    public void addReceiveListener(MsgReceiveListener listener){
        if(listeners == null) {
            listeners = new HashSet<MsgReceiveListener>();
        }
        listeners.add(listener);
    }
    
    public void removeReceiveListener(MsgReceiveListener listener){
        if(listeners == null){
            return;
        }
        listeners.remove(listener);
    }
    
    protected void activeReceiveEvent(String msg){
        if(listeners == null) {
            return;
        }
        MsgReceiveEvent event = new MsgReceiveEvent(this, msg);
        notifyListeners(event);
    }
    
    private void notifyListeners(MsgReceiveEvent event){
        Iterator<MsgReceiveListener> iter = listeners.iterator();
        while(iter.hasNext()){
            MsgReceiveListener listener = (MsgReceiveListener) iter.next();
            listener.receiveMsgEvent(event);
        }
    }
    
}
