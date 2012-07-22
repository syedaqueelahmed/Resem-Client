package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
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

public class MemberSelect extends Activity implements OnItemClickListener{
	

	String familyaadhaarids[]=null;
	static String response=null;
	String aadhaarid=null;
	String selectedfamilyaadhaarid=null;
	DatabaseInteraction d=new DatabaseInteraction(this);
	
	TextView errormessage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memberselect);
		
		Button me=(Button)findViewById(R.id.me),tryagain=(Button)findViewById(R.id.tryagain);
		ListView lv=(ListView) findViewById(R.id.memberlist);
		errormessage=(TextView)findViewById(R.id.errormessage);
		String relations[]=null;
		
		//get local aadhaarid code
		try {
			aadhaarid=URLEncoder.encode(d.query(),"UTF-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			aadhaarid=URLEncoder.encode(d.query());
		}
				
		Thread memberchoice=new Thread(){
			public void run(){
				String servletpath ="FamilyMemberChoice?aadhaarid="+aadhaarid;
				response = ConnectToServer.sendRequest(servletpath);
			}
		};
		memberchoice.start();
		try {
			memberchoice.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		if(response.endsWith("Please try again later.")){
			System.out.println("response.endsWith(Please try again later.)");
			me.setVisibility(View.GONE);
			lv.setVisibility(View.GONE);
			errormessage.setVisibility(View.VISIBLE);
			errormessage.setText(response);
			tryagain.setVisibility(View.VISIBLE);
			tryagain.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					errormessage.setText("Reconnecting");
					v.setVisibility(View.GONE);
					Intent i = new Intent(MemberSelect.this, MemberSelect.class);
					startActivity(i);
				}
			});
		}
		
		
		try {
			JSONArray reply=new JSONArray(response);
			relations = new String[reply.length()];
			familyaadhaarids=new String[reply.length()];
			JSONObject obj;
			for(int i=0;i<reply.length();i++){
				obj=reply.getJSONObject(i);
				familyaadhaarids[i]=obj.getString("familyaadhaarid");
				relations[i]=obj.getString("relation");
				System.out.println(familyaadhaarids[i]+":"+relations[i]);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(MemberSelect.this,android.R.layout.simple_list_item_1, relations);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(this);
		me.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					selectedfamilyaadhaarid=URLEncoder.encode(aadhaarid,"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					selectedfamilyaadhaarid=URLEncoder.encode(aadhaarid);
				}
				System.out.println(selectedfamilyaadhaarid);
				
				Thread sendfamilyaadhaarid=new Thread(){
					public void run(){
						String servletpath ="EmergencyServlet?familyaadhaarid="+selectedfamilyaadhaarid;
						response = ConnectToServer.sendRequest(servletpath);
					}
				};
				sendfamilyaadhaarid.start();
				try {
					sendfamilyaadhaarid.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(response);
				
				if(!response.endsWith("Please try again later.")){
					Acknowledgement.msg=response;
					Intent i=new Intent(MemberSelect.this, Acknowledgement.class);
					startActivity(i);
				}
				else{
					Toast.makeText(MemberSelect.this, response, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		try {
			selectedfamilyaadhaarid=URLEncoder.encode(familyaadhaarids[position],"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			selectedfamilyaadhaarid=URLEncoder.encode(familyaadhaarids[position]);
		}
		System.out.println(selectedfamilyaadhaarid);
		
		Thread sendfamilyaadhaarid=new Thread(){
			public void run(){
				String servletpath ="EmergencyServlet?familyaadhaarid="+selectedfamilyaadhaarid;
				response = ConnectToServer.sendRequest(servletpath);
			}
		};
		sendfamilyaadhaarid.start();
		try {
			sendfamilyaadhaarid.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		
		if(!response.endsWith("Please try again later.")){
			Acknowledgement.msg=response;
			Intent i=new Intent(this, Acknowledgement.class);
			startActivity(i);
		}
		else{
			Toast.makeText(this, response, Toast.LENGTH_LONG).show();
		}
	}
}