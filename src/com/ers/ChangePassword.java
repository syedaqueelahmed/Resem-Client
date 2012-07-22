package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePassword extends Activity implements OnClickListener {
	
	String response,aadhaarid;
	DatabaseInteraction d=new DatabaseInteraction(this);
	
	Button ok, cancel;
	String currentpassword, newpassword;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        
        aadhaarid=d.query();
        
        ok=(Button)findViewById(R.id.ok);
        cancel=(Button)findViewById(R.id.cancel);
        
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
	}
	
	public void onClick(View button) {
		if(button==cancel){
			Intent i=new Intent(getApplicationContext(), MyAccount.class);
			startActivity(i);
		}
		else{
			//validations
			currentpassword=((EditText)findViewById(R.id.currentpassword)).getText().toString();
			if(currentpassword.isEmpty()){
				System.out.println("Please enter your Current Password");
				Toast.makeText(this, "Please enter your Current Password", Toast.LENGTH_LONG).show();
				return;
			}
			
	        newpassword=((EditText)findViewById(R.id.newpassword)).getText().toString();
	        if(newpassword.isEmpty()){
				System.out.println("Please enter your new Password");
				Toast.makeText(this, "Please enter your new Password", Toast.LENGTH_LONG).show();
				return;
			}
	        
	        String retypednewpassword=((EditText)findViewById(R.id.retypednewpassword)).getText().toString();
	        if(retypednewpassword.isEmpty()){
				System.out.println("Please reenter your new Password");
				Toast.makeText(this, "Please reenter your new Password", Toast.LENGTH_LONG).show();
				return;
			}
	        
	        if(currentpassword.length()!=4 || newpassword.length()!=4){
				System.out.println("Password must be of exactly 4 characters.");
				Toast.makeText(this, "Please check the Password length. It must be of exactly 4 characters.", Toast.LENGTH_LONG).show();
				return;
			}
	        
	        if(!newpassword.equals(retypednewpassword)){
	        	System.out.println("New Password & Retyped Password donot match");
				Toast.makeText(this, "New Password & Retyped Password donot match", Toast.LENGTH_LONG).show();
				return;
	        }
			
			//get user key
			Thread getuserkey=new Thread(){
				public void run(){
					String servletpath ="GetUserkey?aadhaarid="+aadhaarid;
					response = ConnectToServer.sendRequest(servletpath);
				}
			};
			getuserkey.start();
			try {
				getuserkey.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(response);
			if(!currentpassword.equals(response)){
				System.out.println("Incorrect Existing password");
				Toast.makeText(this, "Incorrect Existing password", Toast.LENGTH_LONG).show();
			}
			else{
				try {
					newpassword=URLEncoder.encode(newpassword,"UTF-8");
				} catch (UnsupportedEncodingException e2) {
					e2.printStackTrace();
					newpassword=URLEncoder.encode(newpassword);
				}
				
				//update user key
				Thread updateuserkey=new Thread(){
					public void run(){
						String servletpath ="UpdateUserkey?aadhaarid="+aadhaarid+"&password="+newpassword;
						response = ConnectToServer.sendRequest(servletpath);
					}
				};
				updateuserkey.start();
				try {
					updateuserkey.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(response);
				
				if(response.equalsIgnoreCase("success")){
					Toast.makeText(getApplicationContext(), "Password Changed", Toast.LENGTH_LONG).show();
					Intent i=new Intent(getApplicationContext(), MyAccount.class);
					startActivity(i);
				}
				else{
					Toast.makeText(this, response, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
}
