package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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

public class EditAcquaintances extends Activity implements OnItemClickListener {

	String aadhaarid,response=null,relations[],familyaadhaarids[],selectedfamilyaadhaarid;
	DatabaseInteraction d=new DatabaseInteraction(this);
	Button addmore,done,tryagain;
	ListView acquaintances;
	TextView errormessage;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editacquaintances);
        
        addmore=(Button)findViewById(R.id.more);
        done=(Button)findViewById(R.id.done);
        acquaintances=(ListView)findViewById(R.id.acquaintances);
        
        done.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				Intent i=new Intent(getApplicationContext(), MyAccount.class);
				startActivity(i);
			}
		});
        addmore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i=new Intent(getApplicationContext(), AcquaintanceDetailsRegistration.class);
				startActivity(i);
			}
		});

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
			errormessage=(TextView)findViewById(R.id.errormessage);
			tryagain=(Button)findViewById(R.id.tryagain);
			
			findViewById(R.id.textView1).setVisibility(View.GONE);
			acquaintances.setVisibility(View.GONE);
			addmore.setVisibility(View.GONE);
			errormessage.setVisibility(View.VISIBLE);
			tryagain.setVisibility(View.VISIBLE);
			
			errormessage.setText(response);
			tryagain.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					errormessage.setText("Reconnecting");
					v.setVisibility(View.GONE);
					Intent i = new Intent(getApplicationContext(), EditAcquaintances.class);
					startActivity(i);
				}
			});
		}
		
		try {
			JSONArray reply=new JSONArray(response);
			relations = new String[reply.length()];
			familyaadhaarids=new String[reply.length()];
			if(!reply.isNull(0)){
				JSONObject obj;
				for(int i=0;i<reply.length();i++){
					obj=reply.getJSONObject(i);
					familyaadhaarids[i]=obj.getString("familyaadhaarid");
					relations[i]=obj.getString("relation");
					System.out.println(familyaadhaarids[i]+":"+relations[i]);
				}
			}
			else{
				acquaintances.setVisibility(View.GONE);
				((TextView)findViewById(R.id.textView1)).setText("You have not enrolled your acquaintances yet.");
				addmore.setText("Enroll now");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, relations);
		acquaintances.setAdapter(adapter);

		acquaintances.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
		Builder sure=new Builder(this);
		sure.setIcon(R.drawable.resem);
		sure.setTitle("Sure?");
		sure.setMessage("Are you sure you want to remove this relationship?");
		sure.setNegativeButton("No", null);
		sure.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					selectedfamilyaadhaarid=URLEncoder.encode(familyaadhaarids[position],"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					selectedfamilyaadhaarid=URLEncoder.encode(familyaadhaarids[position]);
				}
				System.out.println(selectedfamilyaadhaarid);
				
				Thread sendfamilyaadhaarid=new Thread(){
					public void run(){
						String servletpath ="UpdateDetails?formname=family&aadhaarid="+aadhaarid+"&familyaadhaarid="+selectedfamilyaadhaarid;
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
					Intent i=new Intent(getApplicationContext(), EditAcquaintances.class);
					startActivity(i);
				}
				else{
					Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
				}
			}
		});
		sure.show();
	}
}