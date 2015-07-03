package com.rafcarl.lifecycle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ContactItem extends Activity {
	public static final String LOG = "ContactItem";
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_item);
		
		DBHelper.dbHelper = new DBHelper(this);
		DBHelper.db = DBHelper.dbHelper.getReadableDatabase();
		
		intent = getIntent();
		displayInfo(intent.getStringExtra("ID"));
				
		Button saveButton = (Button) findViewById(R.id.saveContact);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				saveContact();
			}
		});
	}
	
	// Populates Contact with their info
	public void displayInfo(String id){
		Cursor select;
		String[] projection = { DBHelper.NAME, DBHelper.NUMBER, DBHelper.MESSAGE };
		String selection = DBHelper.CONTACT_ID + "=?";
		String[] selectionArgs = { id };
		select = DBHelper.db.query(DBHelper.TABLE_NAME, projection, selection,
				selectionArgs, null, null, null);
		if (select.moveToFirst()) {
			EditText temp;
			
			temp = (EditText) findViewById(R.id.editName);
			temp.setText(select.getString(select.getColumnIndex(DBHelper.NAME)));
			
			temp = (EditText) findViewById(R.id.editNumber);
			temp.setText(select.getString(select.getColumnIndex(DBHelper.NUMBER)));
			
			temp = (EditText) findViewById(R.id.editMessage);
			temp.setText(select.getString(select.getColumnIndex(DBHelper.MESSAGE)));
		}
		select.close();
	}

	// User decides which of the supplied phone numbers to use
	public void selectNumber(View v){
		Cursor phone;
		String[] projection = {Phone.NUMBER, Phone.TYPE};
		String selection = Data.CONTACT_ID + "=?";
		String[] selectionArgs = {intent.getStringExtra("ID")};
		String[] temp;
		String number;
		List<String> numbersList = new ArrayList<String>();

		phone = getContentResolver().query(Phone.CONTENT_URI, projection, selection, selectionArgs, null);
		if(phone.moveToFirst()){
			while(!phone.isAfterLast()){
				if(phone.getInt(phone.getColumnIndex(Phone.TYPE)) == Phone.TYPE_MOBILE){
					number = phone.getString(phone.getColumnIndex(Phone.NUMBER));
					numbersList.add(number);
				}
				phone.moveToNext();
			}
			phone.close();
		}
		
		temp = new String[numbersList.size()];
		temp = numbersList.toArray(temp);

		final String[] contactNumbers = temp;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select a number");
		builder.setItems(contactNumbers, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText temp = (EditText) findViewById(R.id.editNumber);
				temp.setText(contactNumbers[which]);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	// Updates contact information in the database
	public void saveContact(){
		EditText temp;
		
		String number;
		temp = (EditText) findViewById(R.id.editNumber);
		number = temp.getText().toString();
		
		if(number.equals("Select a number")){
			Toast.makeText(this,"No number selected", Toast.LENGTH_SHORT).show();
		}
		else{
			String name;
			temp = (EditText) findViewById(R.id.editName);
			name = temp.getText().toString();		
			
			String message;
			temp = (EditText) findViewById(R.id.editMessage);
			message = temp.getText().toString();
			
			ContentValues values = new ContentValues();
			values.put(DBHelper.NAME, name);
			values.put(DBHelper.NUMBER, number);
			values.put(DBHelper.MESSAGE, message);
			
			String selection = DBHelper.CONTACT_ID + "=?";
			String[] selectionArgs = {intent.getStringExtra("ID")};
			DBHelper.db = DBHelper.dbHelper.getWritableDatabase();
			
			int i = DBHelper.db.update(DBHelper.TABLE_NAME, values, selection, selectionArgs);
			if(i == 1){
				Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
				finish();
			}
			else{
				Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_item, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		DBHelper.db.close();
	}
}
