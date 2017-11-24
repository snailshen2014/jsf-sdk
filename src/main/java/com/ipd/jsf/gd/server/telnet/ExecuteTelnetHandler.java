/**
 * Copyright 2004-2048 .
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.gd.server.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * 执行命令telnet类
 *
 */
public class ExecuteTelnetHandler implements TelnetHandler {
    
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ExecuteTelnetHandler.class);
    

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getCommand()
	 */
	@Override
	public String getCommand() {
		return "exec";
	}

	/* (non-Javadoc)
	 * @see telnet.handler.TelnetHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Execute the system command. Exemple: exec top";
	}

    /* (non-Javadoc)
     * @see telnet.handler.TelnetHandler#telnet(io.netty.channel.Channel, java.lang.String)
     */
    @Override
    public String telnet(Channel channel, String message) {
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		String execStr = null;
		StringBuilder sbMsg = new StringBuilder();
		try{
			String osName = System.getProperty("os.name");
			execStr = message;
			Charset charset = Charset.defaultCharset();
			if (osName.toLowerCase().startsWith("windows") && message.equalsIgnoreCase("top")) {
				execStr = System.getenv("windir")
						+ "\\system32\\wbem\\wmic.exe process get Caption,"
						+ "KernelModeTime,UserModeTime,ThreadCount";
				// Caption,KernelModeTime,UserModeTime,ThreadCount,ReadOperationCount,WriteOperationCount
				charset = Charset.forName("gbk");
			} else if (osName.toLowerCase().startsWith("windows")){
				execStr = "cmd.exe /c " + execStr;
				charset = Charset.forName("gbk");
			}
			proc = rt.exec(execStr);
			proc.getOutputStream().close();
			StreamHelper errorStream = new StreamHelper(proc.getErrorStream(), sbMsg, charset);           
			StreamHelper outputStream = new StreamHelper(proc.getInputStream(), sbMsg, charset);
			errorStream.start();
			outputStream.start();
	
			Thread.sleep(2000);
			proc.waitFor();
		} catch(Exception ex){
            LOGGER.error("", ex);
			sbMsg.append(ex.getMessage());
		} finally {
			if(proc != null){
				proc.destroy();
			}
		}
		return sbMsg.toString();
	}

	private class StreamHelper extends Thread {
		
		private InputStream inStream;
		private StringBuilder sbMsg;
		private Charset charset;

		StreamHelper(InputStream inStream, StringBuilder sbMsg, Charset charset) {
			this.inStream = inStream;
			this.sbMsg = sbMsg;
			this.charset = charset;
		}

		public void run() {
			InputStreamReader streamReader = null;
			BufferedReader bufferReader = null;
			try {
				streamReader = new InputStreamReader(inStream, this.charset);
				bufferReader = new BufferedReader(streamReader);
				String line = null;
				while ((line = bufferReader.readLine()) != null) {
					sbMsg.append(line);
					sbMsg.append("\r\n");
				}	
			} catch (IOException ex) {
                LOGGER.error("", ex);
			} finally {
				if(bufferReader != null) {
					try {
						bufferReader.close();
					} catch(Exception ex) {
                        LOGGER.error("", ex);
					}
				}
				
				if(streamReader != null) {
					try {
						streamReader.close();
					} catch(Exception ex) {
                        LOGGER.error("", ex);
					}
				}
				
				if(inStream != null) {
					try {
						inStream.close();
					} catch(Exception ex) {
                        LOGGER.error("", ex);
					}
				}
			}
		}
	}
}