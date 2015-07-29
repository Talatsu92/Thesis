package com.rafcarl.lifecycle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMSTest extends Activity implements LocationListener{

	private static Context context;
	List<Contact> ContactList;
	EditText num, msg;
	TextView lat, lon, addr;
	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	LocationTracker locationTracker;
	double latitude;
	double longitude;
	String latitudeDMS;
	String longitudeDMS;
	long time = 0;
	float distance = 0;
	Location gpsLocation;
	Location networkLocation;
	Location mLocation = null;
	static boolean locationObtained = false;
	
	String streetAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_test);
		double latitude;
		double longitude;
		context = getApplicationContext();
		
		ContactList = new ArrayList<Contact>();
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		locationTracker = new LocationTracker(locationManager,connectivityManager);
		Log.i("LOCTRACKER", ""+locationTracker);

		num = (EditText)findViewById(R.id.numEnt);
		msg = (EditText)findViewById(R.id.msgEnt);
		lat = (TextView)findViewById(R.id.lat_i);
		lon = (TextView)findViewById(R.id.long_i);
		addr = (TextView)findViewById(R.id.addressi);

		Button location = (Button) findViewById(R.id.locationBtn);
		location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(locationTracker.canGetLocation()){
					Log.i("LOCATION", "LOCATION POSSIBLE");
					locationTracker.getLocation();
					Double latt = locationTracker.getLatitude();
					Double lonn = locationTracker.getLongitude();
					Log.i("LATITUDE", ""+latt);
					Log.i("LONGITUDE", ""+lonn);
					lat.setText(Double.toString(latt));
					lon.setText(Double.toString(lonn));
					addr.setText(getStreetAddress());
				}
				else{
					locationTracker.showSettingsAlert();
				}
				
				/*
				NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				
				if(mWifi.isConnected() || mMobile.isConnected()){
					if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, SMSTest.this);
						Log.i("LOG", "requestLocationUpdates(GPS Provider)");
					}
					if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, SMSTest.this);
						Log.i("LOG", "requestLocationUpdates(NetworkProvider)");
					}
				}
				else{
					Toast.makeText(getBaseContext(), "Cannot determine location at this time", Toast.LENGTH_SHORT).show();
				}
				*/
			}
		});
		
		if(locationTracker.canGetLocation()){
			Log.i("LOCATION", "LOCATION POSSIBLE");
			locationTracker.getLocation();
			latitude = locationTracker.getLatitude();
			longitude = locationTracker.getLongitude();
			Log.i("LATITUDE", "" + latitude);
			Log.i("LONGITUDE", "" + longitude);
			lat.setText(Double.toString(latitude));
			lon.setText(Double.toString(longitude));
			addr.setText(getStreetAddress());
		}
		else{
			locationTracker.showSettingsAlert();
		}

		/*
		GPSLocationTrackerTest locationTracker = new GPSLocationTrackerTest(SMSTest.this);
		if(locationTracker.canGetLocation()){
			if(locationTracker.getLocation() == null){
				//Toast.makeText(getApplicationContext(), "Location is NULL", Toast.LENGTH_SHORT).show();
			}
			latitude = locationTracker.getLatitude();
			longitude = locationTracker.getLongitude();
			Log.i("LAT", String.format("Latitude: %s", latitude));
			Log.i("LON", String.format("Longitude: %s", longitude));
			lat.setText(Double.toString(latitude));
			lon.setText(Double.toString(longitude));
			
			Geocoder geocoder;
			List<Address> addresses = null;
			geocoder = new Geocoder(this, Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(addresses != null && addresses.size() > 0){
				Log.i("ADDRESS", addresses.get(0).toString());
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getAddressLine(1);
				String region = addresses.get(0).getAddressLine(2);
				String country = addresses.get(0).getAddressLine(3);

				String fulladdr = address + ", " + city + ", " + region + ", " + country;
				addr.setText(fulladdr);
			}
			else{
				//Toast.makeText(getApplicationContext(), "Address Unknown", Toast.LENGTH_SHORT).show();
			}
		}
		else{
			locationTracker.showSettingsAlert();
		}
		*/
	}

	public void sendSMS(View v)
	{      
		String phoneNumber = num.getText().toString();
		String message = msg.getText().toString();
		
		try{
			SmsManager sms = SmsManager.getDefault();
			String mapsLink = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;
			String additional = "\n\n To view the location on Google Maps, follow this link:  " + mapsLink;
			
			sms.sendTextMessage(phoneNumber, null, message + additional, null, null);
			Toast.makeText(getApplicationContext(), "Wee", Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), "Aww", Toast.LENGTH_SHORT).show();
		}

		/*SmsManager sms = SmsManager.getDefault();
		StringBuilder message;
		String mapsLink = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;
		String additional = "\n\n To view the location on Google Maps, follow this link:  " + mapsLink;
		for(Contact contact : ContactList) {
			phoneNumber = contact.getNumber();
			message = new StringBuilder(contact.getMessage());
			message.append(additional);
			
			Log.i("CONTACT", ""+contact.getName()+";"+phoneNumber);
		}*/
	}
	
	public double[] getCoordinates(){
		Log.i("LOG", "getCoordinates() enter");
		double[] coordinates = new double[2];
		coordinates[0] = latitude;
		coordinates[1] = longitude;
		
		return coordinates;
	}
	
	public void disconnect(){
		locationManager.removeUpdates(this);
	}
	
	public Location checkAccuracy(Location network, Location gps){
		if(network == null){
			return gps;
		}
		else if(gps == null){
			return network;
		}
		
		return (network.getAccuracy() > gps.getAccuracy()) ? network : gps;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(location != null){
			if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
				networkLocation = location;
			}
			if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
				gpsLocation = location;
			}
			locationObtained = true;
		}

		this.mLocation = checkAccuracy(networkLocation, gpsLocation);
		
	}
	
	public String getStreetAddress(){
		Log.i("LOG", "getStreetAddress() enter");

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(addresses != null & addresses.size() > 0){
			Address address = addresses.get(0);
			streetAddress = String.format("%s, %s, %s", address.getMaxAddressLineIndex() > 0 ?
					address.getAddressLine(0) : "", 
					address.getLocality(),
					address.getCountryName());
		}
		
		return streetAddress;
	}
	
	public void getContacts(){
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
				
				ContactList.add(contact);

				cursor.moveToNext();
			}
		}
		cursor.close();
	}
	
	public void convertToDMS(){
		short degree;
		byte minute;
		float second;
		float temp;

		/*degree = (short) ((latitude >= 0) ? Math.floor(latitude) : Math.ceil(latitude));
		temp = (float) ((latitude - degree) * 60);
		minute = (byte) Math.floor(temp);
		temp = (temp - minute) * 60;
		second = Math.round(temp);*/

		degree = (short) ((latitude >= 0) ? Math.floor(latitude) : Math.ceil(latitude));
		temp = (float) (Math.abs(latitude) - Math.abs(degree)) * 60;
		minute = (byte) Math.floor(temp);
		temp -= minute;
		second = temp * 60;
		
		latitudeDMS = degree + "‹" + minute + "'" + second + "\"";
		
		degree = (short) ((longitude >= 0) ? Math.floor(longitude) : Math.ceil(longitude));
		temp = (float) (Math.abs(longitude) - Math.abs(degree)) * 60;
		minute = (byte) Math.floor(temp);
		temp -= minute;
		second = temp * 60;
		
		longitudeDMS = degree + "°" + minute + "'" + second + "\"";
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	public static Context getContext() {
	      //  return instance.getApplicationContext();
	      return context;
	    }
}
