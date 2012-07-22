package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EmergencyType extends Activity implements OnItemClickListener {
	
	String typeids[]=null;
	String response=null;
	String aadhaarid=null;
	static String latitude=null,longitude=null;
	String selectedtypeid=null;
	DatabaseInteraction d=new DatabaseInteraction(this);
	
	TextView errormessage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencytype);
		
		Button more=(Button) findViewById(R.id.more),tryagain=(Button)findViewById(R.id.tryagain);
		Button myaccount=(Button)findViewById(R.id.myaccount);
		ListView lv=(ListView) findViewById(R.id.servicetypes);
		errormessage=(TextView)findViewById(R.id.errormessage);
		String typenames[]=null;

		//get local aadhaarid code
		aadhaarid=d.query();
		
		if(aadhaarid!=null){
			try {
				System.out.println(aadhaarid);
				aadhaarid=URLEncoder.encode(aadhaarid,"UTF-8");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
				aadhaarid=URLEncoder.encode(aadhaarid);
			}
			//get latitude, longitude code
			LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);			
			LocationListener listener= new LocationListener(){
				public void onLocationChanged(Location location) {
					latitude=((Double)location.getLatitude()).toString();
			        longitude=((Double)location.getLongitude()).toString();
			        System.out.println("Current location is: Latitude = "+latitude+", Longitude = "+longitude);
			        Thread locationupdaterthread=new Thread(){
			        	public void run(){
			        		String servletpath ="UserLocationUpdater?aadhaarid="+aadhaarid+"&latitude="+latitude+"&longitude="+longitude;
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
				}							
				public void onProviderDisabled(String provider) {
					Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
				}			
				public void onProviderEnabled(String provider) {
					Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
				}			
				public void onStatusChanged(String provider, int status, Bundle extras) {}
			};
			manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long)1000, (float)1, listener);
			
			Thread servicetypechoicethread=new Thread(){
				public void run(){
					String servletpath ="ServiceTypeChoice";
					response = ConnectToServer.sendRequest(servletpath);
				}
			};
			servicetypechoicethread.start();
			try {
				servicetypechoicethread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(response);
			if(response.endsWith("Please try again later.")){
				System.out.println("response.endsWith(Please try again later.)");
				more.setVisibility(View.GONE);
				myaccount.setVisibility(View.GONE);
				lv.setVisibility(View.GONE);
				errormessage.setVisibility(View.VISIBLE);
				errormessage.setText(response);
				tryagain.setVisibility(View.VISIBLE);
				tryagain.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						errormessage.setText("Reconnecting");
						v.setVisibility(View.GONE);
						Intent i = new Intent(EmergencyType.this, EmergencyType.class);
						startActivity(i);
					}
				});
				return;
			}
				
			try {
				JSONObject reply=new JSONObject(response);
				typenames = new String[reply.length()];
				typeids=new String[reply.length()];
				JSONArray type_ids= reply.names();
				System.out.println(type_ids);
				for (int i = 0; i < reply.length(); i++) {
					for(int j=0; j<reply.length(); j++){
						if(i+1==type_ids.getInt(j)){
							typeids[i]=type_ids.getString(j);
							typenames[i] = reply.getString(typeids[i]);
							System.out.println(typeids[i]+":"+typenames[i]);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
				
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					EmergencyType.this,
					android.R.layout.simple_list_item_1, 
					typenames);
			lv.setAdapter(adapter);
			
			lv.setOnItemClickListener(this);
			more.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(EmergencyType.this, PersonalDetailsRegistration.class);
					startActivity(i);
				}
			});
			
			myaccount.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(EmergencyType.this, MyAccount.class);
					startActivity(i);
				}
			});
		}
		else{
			Intent i=new Intent(EmergencyType.this, PersonalDetailsRegistration.class);
			startActivity(i);
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
	{
		selectedtypeid=typeids[position];
		System.out.println(selectedtypeid);
		
		Thread sendtypeid=new Thread(){
			public void run(){
				String servletpath ="EmergencyServlet?aadhaarid="+aadhaarid+"&typeid="+selectedtypeid;
				response = ConnectToServer.sendRequest(servletpath);
			}
		};
		sendtypeid.start();
		try {
			sendtypeid.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		
		if(response.equalsIgnoreCase("success")){
			Intent i=new Intent(EmergencyType.this, MemberSelect.class);
			startActivity(i);
		}
		else{
			Toast.makeText(EmergencyType.this, response, Toast.LENGTH_LONG).show();
		}
	}
}