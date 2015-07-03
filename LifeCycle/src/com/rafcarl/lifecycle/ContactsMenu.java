package com.rafcarl.lifecycle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ContactsMenu extends ListActivity {
	public static final int PICK_CONTACT = 100;
	public static final int UPDATE_EMERGENCY_CONTACT = 200;
	public static final String LOG = "ContactsMenu";
	public static final String preferenceFile = "com.rafcarl.lifecycle.pref";
	//	public static List<Contact> contacts = new ContactsData().getContacts();
	public static ArrayAdapter<Contact> adapter;
	public List<Contact> ContactList = new ArrayList<Contact>();
	Toast msg;

	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts_menu);

		sharedPref = getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
		editor = sharedPref.edit();


		adapter = new ArrayAdapter<Contact>(
				this, android.R.layout.simple_list_item_1, ContactList);
		setListAdapter(adapter);

		DBHelper.dbHelper = new DBHelper(this);
		DBHelper.values = new ContentValues();

		msg = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		if(sharedPref.getBoolean(Flags.FIRST_RUN, true)){
			firstRun();		
		}
		else{
			getContactsFromDatabase(ContactList);
			adapter.notifyDataSetChanged();
		}

		Cursor cursor;
		DBHelper.db = DBHelper.dbHelper.getReadableDatabase();
		cursor = DBHelper.db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);
		int count = cursor.getCount();
		msg.setText(String.valueOf(count));
		msg.show();
		cursor.close();
	}

	public void firstRun(){
		DBHelper.db = DBHelper.dbHelper.getWritableDatabase();

		for(int i = 1; i <= Flags.getMin(); i++){
			//Set contact info
			Contact contact = new Contact();			
			contact.setName("Emergency Contact " + i);
			contact.setNumber("");
			contact.setMessage("");
			contact.setId(String.valueOf(i));

			//Set database info
			DBHelper.values.clear();
			DBHelper.values.put(DBHelper.NAME, "Emergency Contact " + i);
			DBHelper.values.put(DBHelper.NUMBER, "");
			DBHelper.values.put(DBHelper.MESSAGE, "");
			DBHelper.values.put(DBHelper.CONTACT_ID, String.valueOf(i));

			//Add contact to ListView & database
			ContactList.add(contact);
			editor.putInt(Flags.CONTACT_COUNT, Flags.getMin());
			DBHelper.db.insert(DBHelper.TABLE_NAME, null, DBHelper.values);
			editor.commit();
		}
		adapter.notifyDataSetChanged();

		editor.putInt(Flags.EMERGENCY_CONTACTS, Flags.getMin());
		editor.putInt(Flags.POS, Flags.getPos());
		editor.putBoolean(Flags.FIRST_RUN, false);
		editor.commit();

	}

	public void toggle(View v){
		Flags.toggleDeleteState();
	}

	public void openPhonebook(View v){
		int count = sharedPref.getInt(Flags.CONTACT_COUNT, 0);
		if(count < Flags.getMax()){
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			int requestCode, temp_count, temp_pos;

			temp_count = sharedPref.getInt(Flags.EMERGENCY_CONTACTS, 0);
			temp_pos = sharedPref.getInt(Flags.POS, 0);

			if(temp_count >= 1 && temp_pos <= 3){
				requestCode = UPDATE_EMERGENCY_CONTACT;
			}
			else{
				requestCode = PICK_CONTACT;
			}
			startActivityForResult(intent, requestCode);
		}
		else{
			msg.setText("Maximum of " + Flags.getMax() + " contacts");
			msg.show();
		}
	}

	public void getContactsFromDatabase(List<Contact> list){
		list.clear();

		Cursor cursor;
		DBHelper.db = DBHelper.dbHelper.getReadableDatabase();
		cursor = DBHelper.db.rawQuery("SELECT * FROM LifeCycleTable", null);

		if(cursor.moveToFirst()){
			while(!cursor.isAfterLast()){
				Contact contact = new Contact();
				contact.setName(cursor.getString(cursor.getColumnIndex(DBHelper.NAME)));
				contact.setNumber(cursor.getString(cursor.getColumnIndex(DBHelper.NUMBER)));
				contact.setMessage(cursor.getString(cursor.getColumnIndex(DBHelper.MESSAGE)));
				contact.setId(cursor.getString(cursor.getColumnIndex(DBHelper.CONTACT_ID)));
				
				list.add(contact);

				cursor.moveToNext();
			}
		}
		cursor.close();
	}

	public int ifDuplicate(List<Contact> contactList, String name){
		int result = 0;
		String temp;

		for (Contact item : contactList) {
			temp = item.getName();
			if(name.equals(temp)){
				result = 1;
				break;
			}
		}
		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == Activity.RESULT_OK){

			Uri contactData = data.getData();
			Cursor cursor = getContentResolver().query(contactData, null, null, null, null);

			if(cursor.moveToFirst()){
				String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

				cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);

				if(cursor.moveToFirst()){
					getContactsFromDatabase(ContactList);	

					if(ifDuplicate(ContactList, name) == 0){
						// Set contact info
						Contact contact = new Contact();
						contact.setName(name);
						contact.setId(id);
						contact.setMessage(getString(R.string.generic_message));

						// Set database info
						DBHelper.values.clear();
						DBHelper.values.put(DBHelper.NAME, contact.getName());
						DBHelper.values.put(DBHelper.NUMBER, "Select a number");
						DBHelper.values.put(DBHelper.MESSAGE, contact.getMessage());
						DBHelper.values.put(DBHelper.CONTACT_ID, contact.getId());

						DBHelper.db = DBHelper.dbHelper.getWritableDatabase();

						if(requestCode == PICK_CONTACT){
							// Adds the contact into the ListView & database
							DBHelper.db.insert(DBHelper.TABLE_NAME, null, DBHelper.values);
							ContactList.add(contact);		

							//	Increment Contact count
							int count = sharedPref.getInt(Flags.CONTACT_COUNT, 0);
							count++;
							editor.putInt(Flags.CONTACT_COUNT, count);
							editor.commit();
						}
						else if(requestCode == UPDATE_EMERGENCY_CONTACT){ // Will overwrite initial "Emergency Contacts"
							String pos = String.valueOf(sharedPref.getInt(Flags.POS, 0));

							String selection = DBHelper.CONTACT_ID + "=?";
							String[] selectionArgs = {pos};
							DBHelper.db.update(DBHelper.TABLE_NAME, DBHelper.values, selection, selectionArgs);

							int num = sharedPref.getInt(Flags.POS, 0);

							ContactList.remove(num - 1);
							ContactList.add(num - 1, contact);

							//	Flags.pos++;
							num++;
							editor.putInt(Flags.POS, num);

							//	Decrement "Emergency Contacts" count
							num = sharedPref.getInt(Flags.EMERGENCY_CONTACTS, 0);
							num--;
							editor.putInt(Flags.EMERGENCY_CONTACTS, num);
							editor.commit();
						}
						cursor.close();
						adapter.notifyDataSetChanged();

						msg.setText(name + " added");
					}
					else{
						msg.setText(name + " already added");
					}
					msg.show();
				}
			}
		}
	}


	public void displayContactDeleteDialog(Contact contact, int position){
		final String id = contact.getId();
		final String name = contact.getName();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("LifeCycle");
		builder.setMessage("Are you sure you want to delete this contact?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				DBHelper.db =  DBHelper.dbHelper.getWritableDatabase();
				String selection = DBHelper.CONTACT_ID + "=?";
				String[] selectionArgs = {id};
				DBHelper.db.delete(DBHelper.TABLE_NAME, selection, selectionArgs);

				//	Decrement Contact count
				int count = sharedPref.getInt(Flags.CONTACT_COUNT, 0);
				count--;
				editor.putInt(Flags.CONTACT_COUNT, count);
				editor.commit();

				getContactsFromDatabase(ContactList);
				adapter.notifyDataSetChanged();

				msg.setText(name + " deleted");
				msg.show();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Contact contact = (Contact) l.getItemAtPosition(position);

		// Delete toggle is off
		if (Flags.getToggleState() == 0) {
			Intent intent = new Intent(this, ContactItem.class);
			intent.putExtra("ID", contact.getId());
			startActivity(intent);
		}
		// Delete toggle is on
		else{
			if(sharedPref.getInt(Flags.CONTACT_COUNT, 0) == 1){
				msg.setText("Must have at least one contact");
				msg.show();
			}
			else{
				displayContactDeleteDialog(contact, position);
				ToggleButton deleteButton = (ToggleButton) findViewById(R.id.deleteContact);
				deleteButton.toggle();
				Flags.toggleDeleteState();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts_menu, menu);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(item.getItemId() == android.R.id.home){
			finish();
		}
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
