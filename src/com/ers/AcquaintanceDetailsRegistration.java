package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AcquaintanceDetailsRegistration extends Activity implements OnClickListener{
    String response=null;
    String familyaadhaarid,relation,aadhaarid;
    Button more,finish;
    ProgressBar bar;
    DatabaseInteraction d=new DatabaseInteraction(this);
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acquaintancedetailsregistration);
        
        aadhaarid=d.query();
        
        bar=(ProgressBar)findViewById(R.id.progressBar1);
		more=(Button) findViewById(R.id.more);
	    more.setOnClickListener(this);
	    finish=(Button) findViewById(R.id.finish);
	    finish.setOnClickListener(this);
    }
    
	public void onClick(View v) {
		familyaadhaarid=((EditText)findViewById(R.id.familyaadhaarid)).getText().toString().trim();
		relation=((EditText)findViewById(R.id.relation)).getText().toString().trim();
		if(familyaadhaarid.isEmpty() && relation.isEmpty()){
			if(v==finish){
				Builder confirm = new Builder(this);
				confirm.setIcon(R.drawable.resem);
				confirm.setTitle("Sure?");
				confirm.setMessage("Are you sure you don't want to enroll anymore of your acquaintances?" +
				 		"\nYou can always enroll them later by accessing your account settings.");
				confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						bar.setProgress(bar.getMax());
						Toast.makeText(getApplicationContext(), "Registration Successful.", Toast.LENGTH_LONG).show();
						Intent i=new Intent(AcquaintanceDetailsRegistration.this, EmergencyType.class);
						startActivity(i);
					}
				});
				confirm.setNegativeButton("No", null);
				confirm.show();
				return;
			}
		}
		if(familyaadhaarid.isEmpty()){
			System.out.println("Please enter your Acquaintance's Aadhaar ID.");
			Toast.makeText(this, "Please enter your Acquaintance's Aadhaar ID.", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			familyaadhaarid=URLEncoder.encode(familyaadhaarid,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			familyaadhaarid=URLEncoder.encode(familyaadhaarid);
		}
		
		if(relation.isEmpty()){
			System.out.println("Please enter your Relationship with the person.");
			Toast.makeText(this, "Please enter your Relationship with the person.", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			relation=URLEncoder.encode(relation,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			relation=URLEncoder.encode(relation);
		}
		
		Thread registerfamily=new Thread(){
			public void run(){
				String servletpath ="RegistrationServlet?0=family&1="+aadhaarid+"&2="+familyaadhaarid+"&3="+relation;
				response = ConnectToServer.sendRequest(servletpath);
			}
		};
		registerfamily.start();
		try {
			registerfamily.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		
		if(response.equalsIgnoreCase("success")){
			Toast.makeText(this, "Acquaintance Registered", Toast.LENGTH_LONG).show();
		}
		else{
			Toast.makeText(this,response, 5000).show();
			return;
		}
		
		if(v == more)
		{
			Intent i=new Intent(this,AcquaintanceDetailsRegistration.class);
			startActivity(i);
		}
		if(v == finish)
		{
			bar.incrementProgressBy(1);
			Builder successful = new Builder(this);
			successful.setIcon(R.drawable.resem);
			successful.setTitle("Registration Successful");
			successful.setCancelable(true);
			//setNeutralButton was not necessary for 4.0.3
			successful.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent i=new Intent(getApplicationContext(), EmergencyType.class);
					startActivity(i);
				}
			});
			//onCancel is not working in 2.3.3 hence neutral is added.
			successful.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					Intent i=new Intent(getApplicationContext(), EmergencyType.class);
					startActivity(i);
				}
			});
			successful.show();
		}
	}
}