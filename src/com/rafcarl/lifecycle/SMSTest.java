package com.rafcarl.lifecycle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	EditText num, msg;
	TextView lat, lon, addr;
	LocationManager locationManager;
	double latitude;
	double longitude;
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

		Button location = (Button) findViewById(R.id.locationBtn);
		location.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
/*				TextView textLong = (TextView) findViewById(R.id.longi);
				TextView textLat = (TextView) findViewById(R.id.lati);
				TextView textAddr = (TextView) findViewById(R.id.addressi);
				TextView textType = (TextView) findViewById(R.id.typei);*/
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				
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
				
/*				LocationTracker locationTracker = new LocationTracker(textLong, textLat, textAddr, textType, locationManager, connectivityManager, SMSTest.this);
				if(locationTracker.canGetLocation()){
					locationTracker.getLocation();
				}
				else{
					Toast.makeText(getBaseContext(), "Cannot determine location at this time", Toast.LENGTH_SHORT).show();
				}
				*/
			}
		});
		
		num = (EditText)findViewById(R.id.numEnt);
		msg = (EditText)findViewById(R.id.msgEnt);
		lat = (TextView)findViewById(R.id.lati);
		lon = (TextView)findViewById(R.id.longi);
		addr = (TextView)findViewById(R.id.addressi);

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
	}

	public void sendSMS(View v)
	{      
		String phoneNumber = num.getText().toString();
		String message = msg.getText().toString();
		
		try{
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, null, null);
			Toast.makeText(getApplicationContext(), "Wee", Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), "Aww", Toast.LENGTH_SHORT).show();
		}
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
		latitude = mLocation.getLatitude();
		longitude = mLocation.getLongitude();

		Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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
