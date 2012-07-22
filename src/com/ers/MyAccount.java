package com.ers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MyAccount extends Activity implements OnClickListener {
	
	String userkey,bloodgroup,aadhaarid,insuranceid;
	String bloodgroups[]={"A+","A-","B+","B-","AB+","AB-","O+","O-"},firmnames[];
	int selectedfirmid;
    Button done,changepassword, editacquaintances,cancel;
    EditText insurance_id,password;
    String response=null;
    DatabaseInteraction d=new DatabaseInteraction(this);
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myaccount);
        
        Spinner blood_group=(Spinner) findViewById(R.id.bloodgroup);
        Spinner insurancefirms=(Spinner) findViewById(R.id.insurancefirms);        
        insurance_id=(EditText)findViewById(R.id.insuranceid);
        password=(EditText)findViewById(R.id.password);
        
        //putting the user's aadhaarid
        aadhaarid=d.query();
        ((TextView)findViewById(R.id.aadhaarid)).setText(aadhaarid);        
        
        //putting blood groups into spinner
        ArrayAdapter<String> adapter_bloodgroups = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, bloodgroups);
        blood_group.setAdapter(adapter_bloodgroups);        
        Thread getbloodgroup=new Thread(){
        	public void run(){
        		String servletpath="GetDetails?formname=personal&aadhaarid="+aadhaarid;
        		response=ConnectToServer.sendRequest(servletpath);
        	}
        };
        getbloodgroup.start();
        try{
        	getbloodgroup.join();
        }
        catch(InterruptedException e){
			e.printStackTrace();        	
        }
        //selecting the user's blood group
        for(int i=0;i<bloodgroups.length;i++){
        	if(bloodgroups[i].equalsIgnoreCase(response)){
        		blood_group.setSelection(i);
        		System.out.println(bloodgroups[i]);
        		break;
        	}
        }
        
        //getting insurance firms
        Thread getinsurancefirms=new Thread(){
			public void run(){
				String servletpath ="InsuranceChoice";
				response = ConnectToServer.sendRequest(servletpath);
			}
		};
		getinsurancefirms.start();
		try {
			getinsurancefirms.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		try {
			JSONObject reply=new JSONObject(response);
			firmnames = new String[reply.length()];
			for (int i = 0; i < reply.length(); i++) {
					firmnames[i] = reply.getString(new Integer(i+1).toString());
					System.out.println((i+1)+":"+firmnames[i]);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//putting insurance firms into spinner
		ArrayAdapter<String> adapter_insurancefirms = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, firmnames);
		insurancefirms.setAdapter(adapter_insurancefirms);
		//getting the user's insurance id and firm
		Thread getinsurance=new Thread(){
        	public void run(){
        		String servletpath="GetDetails?formname=insurance&aadhaarid="+aadhaarid;
        		response=ConnectToServer.sendRequest(servletpath);
        	}
        };
        getinsurance.start();
        try{
        	getinsurance.join();
        }
        catch(InterruptedException e){
			e.printStackTrace();        	
        }
        System.out.println(response);
		JSONObject reply;
		try {
			reply = new JSONObject(response);
			insurancefirms.setSelection(reply.getInt("firmid")-1);
			insurance_id.setText(reply.getString("insuranceid"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
		blood_group.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				bloodgroup=bloodgroups[position];
				System.out.println(bloodgroup);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(getApplicationContext(), "Select a Blood Group", Toast.LENGTH_LONG).show();
			}
		});
		
		insurancefirms.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				selectedfirmid=position+1;
				System.out.println(selectedfirmid);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(getApplicationContext(), "Select an Insurance Firm", Toast.LENGTH_LONG).show();
			}
		});        
        
		cancel=(Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
		done=(Button) findViewById(R.id.done);
	    done.setOnClickListener(this);
	    changepassword=(Button)findViewById(R.id.changepassword);
	    changepassword.setOnClickListener(this);
	    editacquaintances=(Button)findViewById(R.id.editacquaintances);
	    editacquaintances.setOnClickListener(this);
    }

	public void onClick(View button) {
		if(button==done){
			insuranceid=insurance_id.getText().toString();
			//validations
			if(insuranceid.isEmpty()){
				System.out.println("Please enter your new Insurance ID");
				Toast.makeText(this, "Please enter your new Insurance ID", Toast.LENGTH_LONG).show();
				return;
			}
			try {
				insuranceid=URLEncoder.encode(insuranceid,"UTF-8");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
				insuranceid=URLEncoder.encode(insuranceid);
			}
			
			userkey=password.getText().toString();
			if(userkey.isEmpty()){
				System.out.println("Please enter your Current Password.");
				Toast.makeText(this, "Please enter your Current Password.", Toast.LENGTH_LONG).show();
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
			if(!userkey.equals(response)){
				Builder incorrectpassword = new Builder(this);
				incorrectpassword.setIcon(R.drawable.resem);
				incorrectpassword.setTitle("Incorrect Password!");
				incorrectpassword.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "Your information has not been changed", Toast.LENGTH_LONG).show();
						Intent i=new Intent(getApplicationContext(), EmergencyType.class);
						startActivity(i);
					}
				});
				incorrectpassword.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "Enter Correct Password", Toast.LENGTH_LONG).show();
					}
				});
				incorrectpassword.show();
			}
			else{
				
				try {
					bloodgroup=URLEncoder.encode(bloodgroup,"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					bloodgroup=URLEncoder.encode(bloodgroup);
				}
				//update blood group
				Thread updatebloodgroup=new Thread(){
					public void run(){
						String servletpath ="UpdateDetails?formname=personal&aadhaarid="+aadhaarid+"&bloodgroup="+bloodgroup;
						response = ConnectToServer.sendRequest(servletpath);
					}
				};
				updatebloodgroup.start();
				try {
					updatebloodgroup.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(response);
				if(!response.equalsIgnoreCase("success")){
					Toast.makeText(this, response, Toast.LENGTH_LONG).show();
					return;
				}
				
				//update insurance details
				Thread updateinsurance=new Thread(){
					public void run(){
						String servletpath ="UpdateDetails?formname=insurance&aadhaarid="+aadhaarid
								+"&insuranceid="+insuranceid+"&firmid="+selectedfirmid;
						response = ConnectToServer.sendRequest(servletpath);
					}
				};
				updateinsurance.start();
				try {
					updateinsurance.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(response);
				if(response.equalsIgnoreCase("success")){
					Toast.makeText(getApplicationContext(), "Your information has been updated successfully", Toast.LENGTH_LONG).show();
					Intent i=new Intent(this, EmergencyType.class);
					startActivity(i);
				}
				else{
					Toast.makeText(this, response, Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		else if(button==changepassword){
			Intent i=new Intent(this, ChangePassword.class);
			startActivity(i);
		}
		else if(button==editacquaintances){
			Intent i=new Intent(this, EditAcquaintances.class);
			startActivity(i);
		}
		else{
			Intent i=new Intent(this, EmergencyType.class);
			startActivity(i);
		}
	}
}
