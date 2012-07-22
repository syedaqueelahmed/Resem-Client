package com.ers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseInteraction extends SQLiteOpenHelper {
	
	static String databasename="ersdatabase", tablename="userid", columnname="aadhaarid";
	Context context;

	public DatabaseInteraction(Context context) {
		super(context, databasename, null, 1);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {		
		try {
			db.execSQL("CREATE TABLE "+tablename+"("+columnname+" TEXT PRIMARY KEY)");
			System.out.println("table has been created");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	
		db.execSQL("DROP TABLE IF EXISTS "+tablename);
		System.out.println("table dropped");
		onCreate(db);
	}
	
	public String query() {
		SQLiteDatabase db=null;
		db=this.getWritableDatabase();
		String s=null;
		Cursor c = db.rawQuery("select * from "+tablename, null);
		if(c.isAfterLast())
			return s;
		if (c.moveToNext()) {
			s = c.getString(0);
		}
		return s;
	}
	
	public int insert(String aadhaarid) {		
		SQLiteDatabase db = null;
		db = this.getWritableDatabase();
		
		try {
			ContentValues cv = new ContentValues();
			cv.put(columnname, aadhaarid);

			if (db.insert(tablename, null, cv) != -1)
				return -1;
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), 5000).show();
			e.printStackTrace();
		} finally {
			db.close();
		}
		return 1;
	}
}