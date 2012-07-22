package com.ers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Acknowledgement extends Activity {
	static String msg;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acknowledgement);
		
		TextView message=(TextView)findViewById(R.id.message);
		message.setText(msg);
		Button exit=(Button)findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				Intent i=new Intent(Acknowledgement.this, EmergencyType.class);
				startActivity(i);
			}
		});
	}
}
