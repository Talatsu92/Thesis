package com.rafcarl.lifecycle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class Message{
	final private List<Contact> contactList = getContacts();
	String LOG = "Message";
	
	LocationManager locationManager = null;
	ConnectivityManager connectivityManager = null;
	SharedPreferences sharedPref;
	public static final String preferenceFile = "com.rafcarl.lifecycle.prefs";
	public static final String Blood = "bloodKey"; 
	public static final String Anti = "antiKey";
	public static final String Meds = "medKey";
	public static final String Conds = "condtKey";
	public static final String Allergs = "allergKey";
	
	String streetAddress = "";
	double latitude = 0;
	double longitude = 0;
	String latitudeDMS = "";
	String longitudeDMS = "";
	Context context;
		
	public Message(double lat, double lon, Context context){
		latitude = lat;
		longitude = lon;	
		this.context = context;
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
	
	public void sendMessage(Context context){		
		StringBuilder msg = new StringBuilder("");
		SharedPreferences sharedPref = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
		
		StringBuilder blood = new StringBuilder("Blood Type: ");
		if(sharedPref.contains(Blood)){	
			switch(sharedPref.getInt(Blood, 0)){
				case 1: blood.append("O"); break;
				case 2: blood.append("A"); break;
				case 3: blood.append("B"); break;
				case 4: blood.append("AB"); break;
				default: blood.append("");
			}
		}
		if(sharedPref.contains(Anti)){	
			switch(sharedPref.getInt(Anti, 0)){
				case 1: blood.append("+"); break;
				case 2: blood.append("-"); break;
				default: blood.append("");
			}
		}
		
		StringBuilder med = new StringBuilder("Medications: ");
		if(sharedPref.contains(Meds)){
			med.append(sharedPref.getString(Meds, ""));
		}
		
		StringBuilder con = new StringBuilder("Conditions: ");
		if(sharedPref.contains(Conds)){
			con.append(sharedPref.getString(Conds, ""));
		}
		
		StringBuilder allerg = new StringBuilder("Allergies: ");
		if(sharedPref.contains(Allergs)){
			allerg.append(sharedPref.getString(Allergs, ""));
		}
		
		SmsManager sms = SmsManager.getDefault();
		BroadcastReceiver br_sent = null;
		BroadcastReceiver br_delivered = null;
		
		String sent = "SMS_SENT";
		String delivered = "SMS_DELIVERED";
		
		PendingIntent sentPi = PendingIntent.getBroadcast(context, 0, new Intent(sent), 0);
		PendingIntent deliveredPi = PendingIntent.getBroadcast(context, 0, new Intent(delivered), 0);
		
		ArrayList<String> parts = new ArrayList<String>();
		ArrayList<PendingIntent> sentPis = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveredPis = new ArrayList<PendingIntent>();
		
		Contact contact = null;
		
		String phoneNumber = "";
		String mapsLink = "http://maps.google.com/maps?daddr=" + String.valueOf(latitude)+ "," + String.valueOf(longitude) + "";
		String location = "My location: " + mapsLink;
		
		try {			
			br_sent = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					switch(getResultCode()){
					case Activity.RESULT_OK:
						Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(context, "Generic failure",Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
			
			br_delivered = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					switch (getResultCode())
					{
					case Activity.RESULT_OK:
						Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
						break;                        
					}
				}
			};
			
			context.registerReceiver(br_sent, new IntentFilter(sent));
			context.registerReceiver(br_delivered, new IntentFilter(delivered));
			
			for(int i = 0; i < contactList.size(); i++) {
				contact = contactList.get(i);
				phoneNumber = contact.getNumber();
								
//				parts.clear();
//				sentPis.clear();
//				deliveredPis.clear();
//				msg.delete(0, msg.length());
//				
//				msg.append(contact.getMessage() + "\n");
//				msg.append(location + "\n");
//				msg.append(blood.toString() + "\n");
//				msg.append(med.toString() + "\n");
//				msg.append(con.toString() + "\n");
//				msg.append(allerg.toString());
				
				
//				parts = sms.divideMessage(msg.toString());
				
				
//				for(int j = 0; j < parts.size(); j++){
//					sentPis.add(sentPi);
//					deliveredPis.add(deliveredPi);
//				}
				
//				sms.sendMultipartTextMessage(phoneNumber, null, parts, sentPis, deliveredPis);
				sms.sendTextMessage(phoneNumber, null, contact.getMessage(), sentPi, deliveredPi);
				sms.sendTextMessage(phoneNumber, null, location, sentPi, deliveredPi);
				sms.sendTextMessage(phoneNumber, null, blood.toString() + "\n" + med.toString(), sentPi, deliveredPi);
				sms.sendTextMessage(phoneNumber, null, con.toString() + "\n" + allerg.toString(), sentPi, deliveredPi);
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public List<Contact> getContacts(){
		Cursor cursor = null;
		
		List<Contact> contactList = new ArrayList<Contact>();
		
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
		
		return contactList;
	}
	
}
