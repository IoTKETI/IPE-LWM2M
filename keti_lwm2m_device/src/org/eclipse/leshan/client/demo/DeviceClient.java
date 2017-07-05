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
