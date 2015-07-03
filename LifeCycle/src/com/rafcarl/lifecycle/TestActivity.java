package com.rafcarl.lifecycle;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class TestActivity extends Activity {

	DBHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		dbHelper = new DBHelper(this);
		
		SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
	}

}
