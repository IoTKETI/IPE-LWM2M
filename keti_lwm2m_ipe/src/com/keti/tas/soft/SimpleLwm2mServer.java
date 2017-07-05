package com.keti.tas.soft;

import java.math.BigInteger;
import java.net.BindException;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.ResourceModel.Operations;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.codec.DefaultLwM2mNodeDecoder;
import org.eclipse.leshan.core.node.codec.DefaultLwM2mNodeEncoder;
import org.eclipse.leshan.core.node.codec.LwM2mNodeDecoder;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.demo.servlet.ClientServlet;
import org.eclipse.leshan.server.demo.servlet.EventServlet;
import org.eclipse.leshan.server.demo.servlet.ObjectSpecServlet;
import org.eclipse.leshan.server.demo.servlet.SecurityServlet;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeDeserializer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.RegistrationSerializer;
import org.eclipse.leshan.server.demo.servlet.json.ResponseSerializer;
import org.eclipse.leshan.server.impl.FileSecurityStore;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StandardModelProvider;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.leshan.server.security.EditableSecurityStore;
import org.eclipse.leshan.util.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keti.tas.conf.ConfLoader;

public class SimpleLwm2mServer {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleLwm2mServer.class);
    
    private static LeshanServer lwServer;
    private static GsonBuilder gsonBuilder = new GsonBuilder(); 
    
    private Gson gson;
    private MsgReceiveListener handler;
   
    
    public void setMsgReceiver(MsgReceiveListener handler){
    	this.handler = handler;
    }
    
    private void emitValue(String path, String content){
        
        Document document = DocumentHelper.createDocument();
        Element eleRoot = document.addElement("msg");
        
        Element elePath = eleRoot.addElement("path");
        Element eleContent = eleRoot.addElement("value");
        
        elePath.setText(path);
        eleContent.setText(content);

        String msg = document.asXML();
        
        if(handler != null){
        	MsgReceiveEvent event = new MsgReceiveEvent(this, msg);
        	handler.receiveMsgEvent(event);
        }
    }
    
    private void writeOperation(String path, Object value, ResourceModel rm){
    	try{
	    	String[] path_ary = path.split("/");
	    	if(path_ary.length == 5){
	    	
	    		String epName = path_ary[1];
	    		String target = path.substring(epName.length() + 1, path.length());
	    		int rsID = Integer.parseInt(path_ary[4]);
	    		
		    	JSONObject root = new JSONObject();
		    	root.put("id", rsID);
				root.put("value", value);
		    	
		    	Registration registration = lwServer.getRegistrationService().getByEndpoint(epName);
		    	ContentFormat contentFormat = ContentFormat.fromName("TLV");
	
	             // create & process request
	            LwM2mNode node = gson.fromJson(root.toString(), LwM2mNode.class);
	            WriteRequest request = new WriteRequest(Mode.REPLACE, contentFormat, target, node);
	            WriteResponse cResponse = lwServer.send(registration, request, 5000);
	    	}
    	}catch(Exception exp){
    		exp.printStackTrace();
    	}
    }
    
    private void excutionOperation(String path){
    	try{
	    	String[] path_ary = path.split("/");
	    	if(path_ary.length == 5){
	    	
	    		String epName = path_ary[1];
	    		String target = path.substring(epName.length() + 1, path.length());
	    		//int rsID = Integer.parseInt(path_ary[4]);
		    	
		    	Registration registration = lwServer.getRegistrationService().getByEndpoint(epName);
		    	//ContentFormat contentFormat = ContentFormat.fromName("TLV");
	
	             // create & process request
                ExecuteRequest request = new ExecuteRequest(target, "");
                ExecuteResponse cResponse = lwServer.send(registration, request, 5000);
	    	}
    	}catch(Exception exp){
    		exp.printStackTrace();
    	}
    }
    
    public void controlDevice(String path, Object value){
		
		ResourceModel mModel = getResourceModelInfo(path);
		
		if(mModel.operations == Operations.RW 
				|| mModel.operations == Operations.W){
			writeOperation(path, value, mModel);
		} else if(mModel.operations == Operations.E
				|| mModel.operations == Operations.RE){
			excutionOperation(path);
		}
    }

    private final RegistrationListener registrationListener = new RegistrationListener() {

        @Override
        public void registered(Registration registration) {
  
			System.out.println(registration.getEndpoint() + " is registrered!");
			System.out.println("hava resource: ");
			
			for(int i = 0; i < registration.getObjectLinks().length; i ++){
				System.out.println("\t" + registration.getObjectLinks()[i].getUrl());
				if(!registration.getObjectLinks()[i].getUrl().equals("/")){
					String lwm2mpath = "/"+ registration.getEndpoint() + registration.getObjectLinks()[i].getUrl();
					
					try {
						if (registration != null) {
							ContentFormat contentFormat = ContentFormat.fromName("JSON");
							// create & process request
					        for(int j = 0; j < ConfLoader.uploads.size();j++){
					        	 String path = ConfLoader.uploads.get(j).getLwM2MPath();
					        	 if(path.startsWith(lwm2mpath)){
					        		 
						        		 String observationpath = path.substring(registration.getEndpoint().length() + 1, path.length());
						                 
						        		 ObserveRequest request = new ObserveRequest(contentFormat, observationpath);
						                 ObserveResponse cResponse = lwServer.send(registration, request, 5000);
						                 
						                 emitValue(path, cResponse.getContent().toString());
						                 
						                 System.out.println("Observer Response: " + cResponse.toString());
					                 }
					            }     
					       } 
				     } catch (RuntimeException | InterruptedException e){
				    	 
				     }
				}
			}
        }

        @Override
        public void updated(RegistrationUpdate update, Registration updatedRegistration) {
        	System.out.println(updatedRegistration.getEndpoint() + " is update!");
        }

        @Override
        public void unregistered(Registration registration, Collection<Observation> observations) {
        	System.out.println(registration.getEndpoint() + " is unregistered!");
        }
    };

    private final ObservationListener observationListener = new ObservationListener() {

        @Override
        public void cancelled(Observation observation) {
        	
        }

        @Override
        public void onResponse(Observation observation, Registration registration, ObserveResponse response) {
            if (registration != null) {      
                System.out.println("Receive a new sensing data from " + observation.getPath() + " [" + response.getContent() + "]");
                
                String lwm2mpath = "/"+ registration.getEndpoint() + observation.getPath();
                
                emitValue(lwm2mpath, response.getContent().toString());
            }
        }

        @Override
        public void onError(Observation observation, Registration registration, Exception error) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Unable to handle notification of [%s:%s]", observation.getRegistrationId(),
                        observation.getPath()), error);
            }
        }

        @Override
        public void newObservation(Observation observation, Registration registration) {
        }
    };
    
    public void start() {
        // get local address
        int webPort = 8080;

        try {
        	 // Prepare LWM2M server
            LeshanServerBuilder builder = new LeshanServerBuilder();
            builder.setLocalAddress(ConfLoader.CoapHost, ConfLoader.CoapPort);
            builder.setLocalSecureAddress(ConfLoader.CoapsHost, ConfLoader.CoapsPort);
            builder.setEncoder(new DefaultLwM2mNodeEncoder());
            LwM2mNodeDecoder decoder = new DefaultLwM2mNodeDecoder();
            builder.setDecoder(decoder);

            // Get public and private server key
            PrivateKey privateKey = null;
            PublicKey publicKey = null;
            try {
                // Get point values
                byte[] publicX = Hex
                        .decodeHex("fcc28728c123b155be410fc1c0651da374fc6ebe7f96606e90d927d188894a73".toCharArray());
                byte[] publicY = Hex
                        .decodeHex("d2ffaa73957d76984633fc1cc54d0b763ca0559a9dff9706e9f4557dacc3f52a".toCharArray());
                byte[] privateS = Hex
                        .decodeHex("1dae121ba406802ef07c193c1ee4df91115aabd79c1ed7f4c0ef7ef6a5449400".toCharArray());

                // Get Elliptic Curve Parameter spec for secp256r1
                AlgorithmParameters algoParameters = AlgorithmParameters.getInstance("EC");
                algoParameters.init(new ECGenParameterSpec("secp256r1"));
                ECParameterSpec parameterSpec = algoParameters.getParameterSpec(ECParameterSpec.class);

                // Create key specs
                KeySpec publicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(publicX), new BigInteger(publicY)),
                        parameterSpec);
                KeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateS), parameterSpec);

                // Get keys
                publicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
                privateKey = KeyFactory.getInstance("EC").generatePrivate(privateKeySpec);
                builder.setPublicKey(publicKey);
                builder.setPrivateKey(privateKey);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidParameterSpecException e) {
                LOG.error("Unable to initialize RPK.", e);
                System.exit(-1);
            }

            // Define model provider
            LwM2mModelProvider modelProvider = new StandardModelProvider();
            builder.setObjectModelProvider(modelProvider);

            // Set securityStore & registrationStore
            EditableSecurityStore securityStore = new FileSecurityStore();;
            builder.setSecurityStore(securityStore);

            // Create and start LWM2M server
            lwServer = builder.build();
            
            //Add by ChenNan
            gsonBuilder.registerTypeHierarchyAdapter(Registration.class, new RegistrationSerializer(lwServer.getSecureAddress().getPort()));
            gsonBuilder.registerTypeHierarchyAdapter(LwM2mResponse.class, new ResponseSerializer());
            gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
            gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeDeserializer());
            gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            
            this.gson = gsonBuilder.create();

            // Now prepare Jetty
            if(ConfLoader.HttpHost != null){
	            Server server = new Server(webPort);
	            
	            WebAppContext root = new WebAppContext();
	            root.setContextPath("/");
	            //modified by KETI chennan(next code can not work fine in raspberry pi 3)
	            //String url = SimpleLwm2mServer.class.getClassLoader().getResource("webapp").toExternalForm();
	            String user_dir = System.getProperty("user.dir") + "/webapp";
	            System.out.println("Excution path is " + user_dir);
	            root.setResourceBase(user_dir);
	            root.setParentLoaderPriority(true);
	            
	            server.setHandler(root);
	
	            // Create Servlet
	            EventServlet eventServlet = new EventServlet(lwServer, lwServer.getSecureAddress().getPort());
	            ServletHolder eventServletHolder = new ServletHolder(eventServlet);
	            root.addServlet(eventServletHolder, "/event/*");
	
	            ClientServlet clientServlet = new ClientServlet(lwServer, lwServer.getSecureAddress().getPort());
	            ServletHolder clientServletHolder = new ServletHolder(clientServlet);
	            root.addServlet(clientServletHolder, "/api/clients/*");
	
	            SecurityServlet securityServlet = new SecurityServlet(securityStore, publicKey);
	            ServletHolder securityServletHolder = new ServletHolder(securityServlet);
	            root.addServlet(securityServletHolder, "/api/security/*");
	
	            ObjectSpecServlet objectSpecServlet = new ObjectSpecServlet(lwServer.getModelProvider());
	            ServletHolder objectSpecServletHolder = new ServletHolder(objectSpecServlet);
	            root.addServlet(objectSpecServletHolder, "/api/objectspecs/*");
	            
	            //getResourceModelInfo("/sdfsadf/3311/0/5850");
	
	            server.start();
	            LOG.info("Web server started at {}.", server.getURI());
            }
            
            // Start Leshan
            lwServer.start();
            
            lwServer.getObservationService().addListener(observationListener);
            lwServer.getRegistrationService().addListener(registrationListener);

        } catch (BindException e) {
            System.err.println(
                    String.format("Web port %s is alreay used, you could change it using 'webport' option.", webPort));
        } catch (Exception e) {
            LOG.error("Jetty stopped with unexpected error ...", e);
        }
    }
    
    public ResourceModel getResourceModelInfo(String path){
    	ResourceModel mModel = null;
    	try{
    		String[] path_arr = path.split("/");
    		if(path_arr.length == 5){
	    		int object_id = Integer.parseInt(path_arr[2]);
	    		int resource_id = Integer.parseInt(path_arr[4]);
	    		
		    	LwM2mModel model = lwServer.getModelProvider().getObjectModel(null);
		    	ObjectModel[] allmodels = model.getObjectModels().toArray(new ObjectModel[] {});
		    	for(int i = 0; i < allmodels.length; i++){
		    		
		    		if(object_id == allmodels[i].id){
		    			mModel = allmodels[i].resources.get(resource_id);
		    			break;
		    		}
		    	}
    		}
    	}catch (Exception e) {
			// TODO: handle exception
		}
    	return mModel;
    }
    
    public ArrayList<Registration> getAllRegistrationEndPoints(){
    	
    	ArrayList<Registration> registrations = new ArrayList<>();
        for (Iterator<Registration> iterator = lwServer.getRegistrationService().getAllRegistrations(); iterator
                .hasNext();) {
            registrations.add(iterator.next());
        }
        
        return registrations;
    }
}
