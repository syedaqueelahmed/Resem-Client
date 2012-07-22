package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class PersonalDetailsRegistration extends Activity implements OnClickListener {
	String userkey,bloodgroup;
	String bloodgroups[]={"A+","A-","B+","B-","AB+","AB-","O+","O-"};
	static String aadhaarid;
    Button next;
    String response=null;
    boolean aadhaaridexists=false;
    ProgressBar bar;
    DatabaseInteraction d=new DatabaseInteraction(this);
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personaldetailsregistration);
        
        Spinner blood_group=(Spinner) findViewById(R.id.bloodgroup);
        
        if(d.query()!=null){//code for checking aadhaarid from local sqlite
        	aadhaaridexists=true;
        }
        if(!aadhaaridexists){
        	Builder welcome = new Builder(this);
			welcome.setIcon(R.drawable.resem);
			welcome.setTitle("Welcome to Resem");
			welcome.setMessage("The shrewd way to handle your emergencies.");
			welcome.setCancelable(false);
			welcome.setNeutralButton("Register & Start using Resem", null);
			welcome.show();
        }
                
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PersonalDetailsRegistration.this,android.R.layout.simple_spinner_item, bloodgroups);
        blood_group.setAdapter(adapter);
        blood_group.setSelection(6);
        blood_group.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				bloodgroup=bloodgroups[position];
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(PersonalDetailsRegistration.this, "Select a Blood Group", Toast.LENGTH_LONG).show();
			}
		});
        bar=(ProgressBar)findViewById(R.id.progressBar1);
		next=(Button) findViewById(R.id.next);
	    next.setOnClickListener(this);
    }
    
	public void onClick(View v) {
		aadhaarid=((EditText) findViewById(R.id.aadhaarid)).getText().toString().trim();
        userkey=((EditText) findViewById(R.id.password)).getText().toString().trim();
        
		if(aadhaarid.isEmpty()){
			System.out.println("Please enter your Aadhaar ID");
			Toast.makeText(this, "Please enter your Aadhaar ID", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			aadhaarid=URLEncoder.encode(aadhaarid,"UTF-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			aadhaarid=URLEncoder.encode(aadhaarid);
		}
		
		try {
			bloodgroup=URLEncoder.encode(bloodgroup,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			bloodgroup=URLEncoder.encode(bloodgroup);
		}
		
		if(userkey.isEmpty()){
			System.out.println("Please enter the Password.");
			Toast.makeText(this, "Please enter the Password.", Toast.LENGTH_LONG).show();
			return;
		}
		if(userkey.length()!=4){
			System.out.println("Password must be of exactly 4 characters.");
			Toast.makeText(this, "Password must be of exactly 4 characters.", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			userkey=URLEncoder.encode(userkey,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			userkey=URLEncoder.encode(userkey);
		}
			
		Thread registerpersonal=new Thread(){
			public void run(){
				String servletpath ="RegistrationServlet?0=personal&1="+aadhaarid+"&2="+bloodgroup+"&3="+userkey;
				response = ConnectToServer.sendRequest(servletpath);
			}
		};
		registerpersonal.start();
		try {
			registerpersonal.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		
		if(response.equalsIgnoreCase("already registered")){
			Toast.makeText(this,response, Toast.LENGTH_SHORT).show();
			if(aadhaaridexists){
				return;
			}
			Toast.makeText(this,"Checking your Password to match.", Toast.LENGTH_SHORT).show();
			Thread getpassword=new Thread(){
				public void run(){
					String servletpath ="GetUserkey?aadhaarid="+aadhaarid;
					response = ConnectToServer.sendRequest(servletpath);
				}
			};
			getpassword.start();
			try {
				getpassword.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(response);
			try {
				userkey=URLDecoder.decode(userkey, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				userkey=URLDecoder.decode(userkey);
			}
			if(response.equals(userkey)){
				d.insert(aadhaarid);
	        	System.out.println(aadhaarid+" inserted into Sqlite.");
	        	bar.setProgress(bar.getMax());
	        	Builder registrationrestored = new Builder(this);
				registrationrestored.setIcon(R.drawable.resem);
				registrationrestored.setTitle("Welcome back!");
				registrationrestored.setMessage("Password matched.\nYour previously registered info is restored.\nRegistration Successful.");
				registrationrestored.setCancelable(true);
				//setNeutralButton was not necessary for 4.0.3
				registrationrestored.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i=new Intent(getApplicationContext(), EmergencyType.class);
						startActivity(i);
					}
				});
				//onCancel is not working in 2.3.3 hence neutral is added.
				registrationrestored.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						Intent i=new Intent(getApplicationContext(), EmergencyType.class);
						startActivity(i);
					}
				});
				registrationrestored.show();
			}
			else{
				Toast.makeText(this,"Incorrect Password. Please check your Aadhaar ID and Password.", Toast.LENGTH_LONG).show();
			}
		}
		else if(response.equalsIgnoreCase("success")){			
			//location. This code works on 4.0.3 and Google APIs 10. But not on 2.3.3
	        Thread locationgetter=new Thread(){
				public void run(){
					Looper.prepare();
					final Looper my=Looper.myLooper();
					LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);			
					LocationListener listener= new LocationListener(){
						public void onLocationChanged(Location location) {
							EmergencyType.latitude=((Double)location.getLatitude()).toString();
							EmergencyType.longitude=((Double)location.getLongitude()).toString();
					        System.out.println("Current location is: Latitude = "+EmergencyType.latitude+
					        		", Longitude = "+EmergencyType.longitude);
					        my.quit();
						}							
						public void onProviderDisabled(String provider) {
							Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
						}			
						public void onProviderEnabled(String provider) {
							Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
						}			
						public void onStatusChanged(String provider, int status, Bundle extras) {}
					};
					manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long)0, (float)0, listener);
					Looper.loop();
				}
			};
			locationgetter.start();
			try {
				locationgetter.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				Toast.makeText(this, "Error getting location Information. Check your GPS.", Toast.LENGTH_LONG).show();
				return;
			}
			
			Thread locationupdaterthread=new Thread(){
				public void run(){
					String servletpath ="UserLocationUpdater?aadhaarid="+aadhaarid+"&latitude="+
							EmergencyType.latitude+"&longitude="+EmergencyType.longitude;
					response = ConnectToServer.sendRequest(servletpath);
				}
			};
			locationupdaterthread.start();
			try {
				locationupdaterthread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(response);
			if(response.equalsIgnoreCase("success")){
				bar.incrementProgressBy(1);
				if(!aadhaaridexists){
		        	d.insert(aadhaarid);
		        	System.out.println(aadhaarid+" inserted into Sqlite.");
		        }
				Toast.makeText(getApplicationContext(), "Personal details have been registered", Toast.LENGTH_SHORT).show();
				Intent i=new Intent(this, InsuranceDetailsRegistration.class);
				startActivity(i);
			}
			else{
				Toast.makeText(this, response, Toast.LENGTH_LONG).show();
				Thread removeuser=new Thread(){
					public void run(){
						String servletpath ="RemoveUser?aadhaarid="+aadhaarid;
						response = ConnectToServer.sendRequest(servletpath);
					}
				};
				removeuser.start();
				try {
					removeuser.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(response);
				bar.setProgress(1);
				if(!response.equalsIgnoreCase("success"))
					Toast.makeText(this,response+"\nPlease contact us for further assistance.", Toast.LENGTH_LONG).show();
			}
		}
		else{
			Toast.makeText(this,response, Toast.LENGTH_LONG).show();
		}
	}
}