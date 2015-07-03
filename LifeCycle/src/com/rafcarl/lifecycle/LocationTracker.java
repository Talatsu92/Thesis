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
import android.util.Log;

public class LocationTracker extends Activity implements LocationListener{
	String LOG = "LocationTracker";
	Context context;

	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Location gpsLocation;
	Location networkLocation;
	Location mLocation = null;
	static boolean locationObtained = false;

	double latitude;
	double longitude;
	long time = 0;
	float distance = 0;
	
	String streetAddress;

	public LocationTracker(LocationManager locationManager, ConnectivityManager connectivityManager) {
		Log.i(LOG, "LocationTracker constructor enter");
		this.locationManager = locationManager;
		this.connectivityManager = connectivityManager;
//		this.context = context;

		this.gpsLocation = null;
		this.networkLocation = null;
		this.mLocation = null;
	}
	
	public void getLocation(){
		Log.i(LOG, "getLocation() enter");
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, this);
			Log.i(LOG, "requestLocationUpdates(GPS Provider)");
		}
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, this);
			Log.i(LOG, "requestLocationUpdates(NetworkProvider)");
		}
	}
	
	public double[] getCoordinates(){
		Log.i(LOG, "getCoordinates() enter");
		double[] coordinates = new double[2];
		coordinates[0] = latitude;
		coordinates[1] = longitude;
		
		return coordinates;
	}
	
	public void disconnect(){
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {	
		Log.i(LOG, "onLocationChanged() enter");
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

	public Location checkAccuracy(Location network, Location gps){
		if(network == null){
			return gps;
		}
		else if(gps == null){
			return network;
		}
		
		return (network.getAccuracy() > gps.getAccuracy()) ? network : gps;
	}
	
	public String getStreetAddress(){
		Log.i(LOG, "getStreetAddress() enter");
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

//	public void displayAddress(){
//		latitude = mLocation.getLatitude();
//		longitude = mLocation.getLongitude();
//
//		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
//		List<Address> addresses = null;
//		try {
//			addresses = geocoder.getFromLocation(latitude, longitude, 1);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(addresses != null & addresses.size() > 0){
//			Address address = addresses.get(0);
//			String addressText = String.format("%s, %s, %s", address.getMaxAddressLineIndex() > 0 ?
//					address.getAddressLine(0) : "", 
//					address.getLocality(),
//					address.getCountryName());
//
//			textAddr.setText(addressText);
//		}
//	}

	public boolean canGetLocation(){
		Log.i(LOG, "canGetLocation() enter");
		NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		return (mWifi.isConnected() || mMobile.isConnected()) ? true : false;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
