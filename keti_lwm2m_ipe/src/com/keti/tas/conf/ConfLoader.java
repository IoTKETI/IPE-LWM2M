package com.keti.tas.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;


public class ConfLoader {
	
	private final static Logger LOG = Logger.getLogger(ConfLoader.class.getName());
	
	private static String file_path = "tas_conf.xml";
	
	public static String ThymeHost = "localhost";
	public static int ThymePort = 7622;
	public static String HttpHost = "localhost";
	public static int HttpPort = 8080;
	public static String CoapHost = "localhost";
	public static int CoapPort = 5083;
	public static String CoapsHost = "localhost";
	public static int CoapsPort = 5084;
	
	public static ArrayList<DeviceInfo> uploads = new ArrayList<>();
	public static ArrayList<DeviceInfo> downloads = new ArrayList<>();
	
	public static void loadFile (String path){
		
		file_path = path;
		
		parse();
	}
	
	private static String getFileContent(){
		
		String strResult = "";
		
		File file = new File(file_path);
		
		if(file.exists()){
		
			BufferedReader br = null;
			try{
				
				br = new BufferedReader(new FileReader(file));
				
				String strLine = null;
				
				
				while((strLine = br.readLine()) != null){
					if(strLine.trim().length() > 0 ){
						strResult += strLine;
					}
				}
				br.close();
								
			}catch(Exception exp){
				strResult = "";
			}
		}
		
		return strResult;
	}
	
	private static void parse(){
		try{
			String strXML = getFileContent();
			
			Document doc = DocumentHelper.parseText(strXML);
			
			ThymeHost = doc.selectSingleNode("conf/tas/parenthostname").getText();
			ThymePort = Integer.parseInt(doc.selectSingleNode("conf/tas/parentport").getText());
			
			Element eleLwServer = (Element)doc.selectSingleNode("conf/lwm2m/server");
			
			Element eleLwHttpServer = (Element)eleLwServer.selectSingleNode("http");
			Element eleLwCoapServer = (Element)eleLwServer.selectSingleNode("coap");
			Element eleLwCoapsServer = (Element)eleLwServer.selectSingleNode("coaps");
			
			if(eleLwHttpServer!= null){
				HttpHost = eleLwHttpServer.selectSingleNode("host").getText();
				HttpPort = Integer.parseInt(eleLwHttpServer.selectSingleNode("port").getText());
			} else {
				HttpHost = null;
				HttpPort = -1;
			}
			
			if(eleLwCoapServer!= null){
				CoapHost = eleLwCoapServer.selectSingleNode("host").getText();
				CoapPort = Integer.parseInt(eleLwCoapServer.selectSingleNode("port").getText());
			} else {
				CoapHost = null;
				CoapPort = -1;
			}
			
			if(eleLwCoapsServer!= null){
				CoapsHost = eleLwCoapsServer.selectSingleNode("host").getText();
				CoapsPort = Integer.parseInt(eleLwCoapsServer.selectSingleNode("port").getText());
			} else {
				CoapsHost = null;
				CoapsPort = -1;
			}
			
			List<Node> uploadNodes = doc.selectNodes("conf/upload");
			
			uploads.clear();
			
			for(Node node : uploadNodes){
				String ctname = node.selectSingleNode("ctname").getText(); 
				String lwm2mpath = node.selectSingleNode("lwm2mpath").getText();
				
				DeviceInfo device = new DeviceInfo();
				device.setContainerName(ctname);
				device.setLwM2MPath(lwm2mpath);
				
				uploads.add(device);
			}
			
			List<Node> downloadNodes = doc.selectNodes("conf/download");
			
			downloads.clear();
			
			for(Node node : downloadNodes){
				String ctname = node.selectSingleNode("ctname").getText(); 
				String lwm2mpath = node.selectSingleNode("lwm2mpath").getText();
				
				DeviceInfo device = new DeviceInfo();
				device.setContainerName(ctname);
				device.setLwM2MPath(lwm2mpath);
				
				downloads.add(device);
			}
			
		}catch(Exception exp){
			LOG.log(Level.SEVERE, exp.getMessage());
		}
	}
}
