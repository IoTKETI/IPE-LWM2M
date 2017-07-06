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
package org.eclipse.leshan.client.demo;

import static org.eclipse.leshan.LwM2mId.DEVICE;
import static org.eclipse.leshan.LwM2mId.SECURITY;
import static org.eclipse.leshan.LwM2mId.SERVER;
import static org.eclipse.leshan.client.object.Security.noSec;

import java.util.List;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.request.BindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceClient {

	private static final Logger LOG = LoggerFactory.getLogger(DeviceClient.class);
	
    private final static String DEFAULT_ENDPOINT = "KETILwVirtualDevice";
    
    public static void main(final String[] args) {
    	
    	if(ConfigLoader.LoadFile()){
    		
    	    SerialConnector sc = new SerialConnector();
    	    TemperatureSensor tempSensor = new TemperatureSensor(sc);
    	    HumiditySensor humiSensor = new HumiditySensor(sc);
    	    LightActuator lightActuator = new LightActuator(sc);

	        // Get endpoint name
	        String endpoint = DEFAULT_ENDPOINT;
	
	        // Get server URI
	        String serverURI="coap://localhost:5083";
	
//	        byte[] pskIdentity = null;
//	        byte[] pskKey = null;
	
	        // get local address
	        String localAddress = "localhost";
	        int localPort = 6000;
	
	        // get secure local address
	        String secureLocalAddress = "localhost";
	        int secureLocalPort = 6001;
	
	        sc.start();
	
	     // Initialize object list
	        ObjectsInitializer initializer = new ObjectsInitializer();

            initializer.setInstancesForObject(SECURITY, noSec(serverURI, 123));
            initializer.setInstancesForObject(SERVER, new Server(123, 30, BindingMode.U, false));
	        
	        initializer.setClassForObject(DEVICE, KETIVirtualDevice.class);
	        initializer.setInstancesForObject(3303, tempSensor);
	        initializer.setInstancesForObject(3304, humiSensor);
	        initializer.setInstancesForObject(3311, lightActuator);
	        List<LwM2mObjectEnabler> enablers = initializer.create(SECURITY, SERVER, DEVICE, 3303, 3304, 3311);

	        // Create client
	        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
	        builder.setLocalAddress(localAddress, localPort);
	        builder.setLocalSecureAddress(secureLocalAddress, secureLocalPort);
	        builder.setObjects(enablers);
	        final LeshanClient client = builder.build();

	        // Start the client
	        client.start();

	        // De-register on shutdown and stop client.
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            @Override
	            public void run() {
	                client.destroy(true); // send de-registration request before destroy
	            }
	        });
    		
    	} else {
    		LOG.error("Can not found the port.conf file!");
    	}
    }

    public static void createAndStartClient(String endpoint, String localAddress, int localPort,
            String secureLocalAddress, int secureLocalPort, boolean needBootstrap, String serverURI, byte[] pskIdentity,
            byte[] pskKey) {


        
        
    }
}
