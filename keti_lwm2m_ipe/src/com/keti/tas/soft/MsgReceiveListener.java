/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keti.tas.soft;

import java.util.EventListener;

/**
 *
 * @author ChenNan
 */
public interface MsgReceiveListener extends EventListener{
    public void receiveMsgEvent(MsgReceiveEvent event);
}
