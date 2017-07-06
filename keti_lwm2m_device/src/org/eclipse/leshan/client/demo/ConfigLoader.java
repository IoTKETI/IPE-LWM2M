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
