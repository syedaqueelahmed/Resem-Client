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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class InsuranceDetailsRegistration extends Activity implements OnClickListener 
{    
	String insuranceid;
	int selectedfirmid;
	Spinner insurancefirms;
    Button finish,next;
    String response=null;
    String firmnames[]=null;
    ProgressBar bar;
    boolean insuranceregistered=false;
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insurancedetailsregistration);
        
        insurancefirms=(Spinner) findViewById(R.id.insurancefirms);
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
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(InsuranceDetailsRegistration.this,android.R.layout.simple_spinner_item, firmnames);
		insurancefirms.setAdapter(adapter);
		insurancefirms.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				selectedfirmid=position+1;
				System.out.println(selectedfirmid);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(InsuranceDetailsRegistration.this, "Select an Insurance Firm", Toast.LENGTH_LONG).show();
			}
		});
		
		bar=(ProgressBar)findViewById(R.id.progressBar1);
		finish=(Button) findViewById(R.id.finish);
	    finish.setOnClickListener(this);
		next=(Button) findViewById(R.id.next);
	    next.setOnClickListener(this);
    }
    
	public void onClick(View v) 
	{
		if(!insuranceregistered){
			insuranceid=((EditText)findViewById(R.id.insuranceid)).getText().toString().trim();
			
			if(insuranceid.isEmpty()){
				System.out.println("Please enter your Insurance ID.");
				Toast.makeText(this, "Please enter your Insurance ID.", Toast.LENGTH_LONG).show();
				return;
			}
			try {
				insuranceid=URLEncoder.encode(insuranceid,"UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				insuranceid=URLEncoder.encode(insuranceid);
			}
			
			Thread registerinsurance=new Thread(){
				public void run(){
					String servletpath ="RegistrationServlet?0=insurance&1="+insuranceid+"&2="+selectedfirmid+"&3="+PersonalDetailsRegistration.aadhaarid;
					response = ConnectToServer.sendRequest(servletpath);
				}
			};
			registerinsurance.start();
			try {
				registerinsurance.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(response);
			
			if(response.equalsIgnoreCase("success")){
				Toast.makeText(getApplicationContext(), "Insurance details Registered", Toast.LENGTH_LONG).show();
				insuranceregistered=true;
				bar.incrementProgressBy(1);
			}
			else{
				Toast.makeText(this,response, 5000).show();
				return;
			}
		}
		
		if(v == finish)
		{
			Builder confirm = new Builder(this);
			confirm.setIcon(R.drawable.resem);
			confirm.setTitle("Sure?");
			confirm.setMessage("Are you sure about not registering your acquaintances at this moment?" +
			 		"\nYou can always enroll them later by accessing your account settings.");
			confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(getApplicationContext(), "Insurance details Registered", Toast.LENGTH_LONG).show();
					bar.setProgress(bar.getMax());
					Intent i=new Intent(InsuranceDetailsRegistration.this, EmergencyType.class);
					startActivity(i);
				}
			});
			confirm.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent i=new Intent(getApplicationContext(), AcquaintanceDetailsRegistration.class);
					startActivity(i);
				}
			});
			confirm.show();
		}
		
		if(v == next)
		{
			Intent i=new Intent(this, AcquaintanceDetailsRegistration.class);
			startActivity(i);
		}
	}
}
