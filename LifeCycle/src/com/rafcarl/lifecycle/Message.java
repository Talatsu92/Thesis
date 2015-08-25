package com.rafcarl.lifecycle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.telephony.SmsManager;
import android.util.Log;

public class Message extends Activity{
	private List<Contact> contactList = null;
	String LOG = "Message";
	
	LocationManager locationManager = null;
	ConnectivityManager connectivityManager = null;
	
	String streetAddress = "";
	double latitude = 0;
	double longitude = 0;
	String latitudeDMS = "";
	String longitudeDMS = "";
		
	public Message(){
		setContactList(new ArrayList<Contact>());
	}
	
	/*public Message(LocationManager locationManager, ConnectivityManager connectivityManager) {
		ContactList = new ArrayList<Contact>();
		
		this.locationManager = locationManager;
		this.connectivityManager = connectivityManager;
	}*/
	
	//Location-related methods
	public int getLocation(){
		Log.i(LOG, "getLocation() enter");
		LocationTracker locationTracker = new LocationTracker(locationManager, connectivityManager);
		Log.i(LOG, "instantiate new locationTracker object");
		if(locationTracker.canGetLocation()){
			Log.i(LOG, "locationTracker.canGetLocation()");
			locationTracker.getLocation();
			Log.i(LOG, "locationTracker.getLocation()");
			if(locationTracker.mLocation != null){
				Log.i(LOG, "locationTracker.mLocation != null");
				streetAddress = locationTracker.getStreetAddress();
				double[] coordinates = locationTracker.getCoordinates();
				
				latitude = coordinates[0];
				longitude = coordinates[1];
			}
			locationTracker.disconnect();
			
			return 1;
		}
		else{
			return 0;
		}
	}
	
	public void convertToDMS(){
		Log.i(LOG, "convertToDMS()");
		short degree;
		byte minute;
		float second;
		float temp;
		
		degree = (short) ((latitude >= 0) ? Math.floor(latitude) : Math.ceil(latitude));
		temp = (float) (Math.abs(latitude) - Math.abs(degree)) * 60;
		minute = (byte) Math.floor(temp);
		temp -= minute;
		second = temp * 60;
		
		latitudeDMS = degree + "°" + minute + "'" + second + "\"";
		
		degree = (short) ((longitude >= 0) ? Math.floor(longitude) : Math.ceil(longitude));
		temp = (float) (Math.abs(longitude) - Math.abs(degree)) * 60;
		minute = (byte) Math.floor(temp);
		temp -= minute;
		second = temp * 60;
		
		longitudeDMS = degree + "°" + minute + "'" + second + "\"";
		Log.i(LOG, "LatitudeDMS: " + latitudeDMS);
		Log.i(LOG, "LongitudeDMS: " + longitudeDMS);
	}
	
	public void promptUser(AccidentDialog accidentDialog){
		
	}
	
	public void sendMessage(){
		SmsManager sms = SmsManager.getDefault();
		String phoneNumber;
		StringBuilder message;
		String mapsLink = "http://maps.google.com/maps?saddr=" + latitudeDMS + "," + longitudeDMS;
		String additional = "\n\n To view the location on Google Maps, follow this link:  " + mapsLink;
				
		for(Contact contact : getContactList()) {
			phoneNumber = contact.getNumber();
			message = new StringBuilder(contact.getMessage());
			message.append(additional);
			
			sms.sendTextMessage(phoneNumber, null, message.toString(), null, null);
		}
	}
	
	public void getContacts(){
		Cursor cursor = null;
		
		try{
			DBHelper.db = DBHelper.dbHelper.getReadableDatabase();
			cursor = DBHelper.db.rawQuery("SELECT * FROM LifeCycleTable", null);

			if(cursor.moveToFirst()){
				while(!cursor.isAfterLast()){
					Contact contact = new Contact();
					contact.setName(cursor.getString(cursor.getColumnIndex(DBHelper.NAME)));
					contact.setNumber(cursor.getString(cursor.getColumnIndex(DBHelper.NUMBER)));
					contact.setMessage(cursor.getString(cursor.getColumnIndex(DBHelper.MESSAGE)));
					contact.setId(cursor.getString(cursor.getColumnIndex(DBHelper.CONTACT_ID)));
					
					contactList.add(contact);

					cursor.moveToNext();
				}
			}
			cursor.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public List<Contact> getContactList() {
		return contactList;
	}

	public void setContactList(List<Contact> contactList) {
		this.contactList = contactList;
	}
}
