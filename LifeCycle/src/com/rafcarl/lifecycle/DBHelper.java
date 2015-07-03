package com.rafcarl.lifecycle;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABSE_NAME = "LifeCycleDatabase.db";
	protected static final String TABLE_NAME ="LifeCycleTable";
	private static final int DATABASE_VERSION = 1;
	protected static final String UID = "_id";
	protected static final String NAME = "Name";
	protected static final String NUMBER = "Number";
	protected static final String MESSAGE = "Message";
	protected static final String CONTACT_ID = "Contact_ID";
	private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
											+ "" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
											+ "" + NAME + " VARCHAR(50), "
											+ "" + NUMBER + " VARCHAR(12), "
											+ "" + MESSAGE + " VARCHAR(200), "
											+ "" + CONTACT_ID + " INTEGER);";	
	private static final String DROP_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME;
	
	private Context context;
	public static DBHelper dbHelper;
	public static SQLiteDatabase db;
	public static ContentValues values;
	
	public DBHelper(Context context) {
		super(context, DATABSE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {		
		try {
			db.execSQL(CREATE_TABLE);			
		} catch (SQLException e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			db.execSQL(DROP_TABLE);
			onCreate(db);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}
	}
}
