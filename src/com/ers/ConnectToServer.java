package com.ers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ConnectToServer {	
	static String server="http://10.0.2.2:8080/EmergencyServer/";
//	static String server="http://192.168.43.62:8080/EmergencyServer/";
	
	public static String sendRequest(String servletpath)
	{
		System.out.println(server+servletpath);
		
		String response=null;
		InputStream responsestream = null;
		
		try {
			URL servlet_url= new URL(server+servletpath);
			HttpURLConnection connection=(HttpURLConnection) servlet_url.openConnection();
			System.out.println(connection);
			if(connection==null){
				return "Error Connecting to the server. Please try again later.";
			}
			
			connection.setConnectTimeout(5000);
			responsestream=connection.getInputStream();
			
			if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
				System.out.println("Response Message: "+connection.getResponseMessage());
				return connection.getResponseMessage()+"Please try again later.";
			}
			
			int i=0;
			StringBuilder s=new StringBuilder("");
			while((i=responsestream.read())!=-1){
				s.append((char)i);
			}
			responsestream.close();
			connection.disconnect();
			
			response=s.toString().trim();
			
			return response;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "Error Connecting to the server. Please try again later.";
		} catch (IOException e) {
			System.out.println("IOException");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return "Error Connecting to the server. Please try again later.";
		}
	}
}