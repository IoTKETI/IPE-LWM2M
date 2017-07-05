package org.eclipse.leshan.client.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ConfigLoader {
	
	private final static String FILE_NAME = "port.conf";
	
	public static String SerialPort = "COM10";
	public static int BaudRate = 9600;
	
	
	public static boolean LoadFile(){
		boolean result = false;
		
		ArrayList<String> content = new ArrayList<>();
		
		File file = new File(FILE_NAME);
		
		if(file.exists()){
		
			BufferedReader br = null;
			try{
				
				br = new BufferedReader(new FileReader(file));
				
				String strLine = null;
				
				
				while((strLine = br.readLine()) != null){
					if(strLine.trim().length() > 0 ){
						content.add(strLine);
					}
				}
				br.close();
				
				for(int i = 0; i < content.size(); i++){
					if(content.get(i).contains("=")){
						String field = content.get(i).split("=")[0];
						String value = content.get(i).split("=")[1];
						
						if(field.trim().length() > 0 && value.trim().length() > 0){
							if(field.equals("serial")){
								SerialPort = value;
							}else if(field.equals("baudrate")){
								BaudRate = Integer.parseInt(value);
							}
							
							result = true;
						}
					}
				}
								
			}catch(Exception exp){
				result = false;
			}
		} 
		else 
		{
			result = false;
		}
		
		return result;
	}
}
