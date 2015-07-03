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
import android.view.View;
import android.widget.TextView;

public class LocationTracker extends Activity implements LocationListener{
	Context context;

	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Location gpsLocation;
	Location networkLocation;
	Location mLocation;

	double latitude;
	double longitude;
	long time = 0;
	float distance = 0;

	String LOG = "LocationTracker";

	TextView textLong;
	TextView textLat;
	TextView textAddr;
	TextView textType;

	public LocationTracker(TextView textLong, TextView textLat, TextView textAddr, TextView textType, LocationManager locationManager, ConnectivityManager connectivityManager, Context context) {
		this.textLong = textLong;
		this.textLat = textLat;
		this.textAddr = textAddr;
		this.textType = textType;
		this.context = context;
		this.locationManager = locationManager;
		this.connectivityManager = connectivityManager;

		gpsLocation = null;
		networkLocation = null;
		mLocation = null;
	}
	
	public void getLocation(){
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, this);
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, this);
		}
	}

	public void disconnect(){
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		if(location != null){
			if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
				networkLocation = location;
			}
			if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
				gpsLocation = location;
			}
		}

		mLocation = checkAccuracy(networkLocation, gpsLocation);

		if(networkLocation != null){
			textType.setText("network");
		}
		if(gpsLocation != null){
			textType.setText("gps");
		}

		textLong.setText(Double.toString(mLocation.getLongitude()));
		textLat.setText(Double.toString(mLocation.getLatitude()));

		displayAddress();
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

	public void displayAddress(){
		latitude = mLocation.getLatitude();
		longitude = mLocation.getLongitude();

		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(addresses != null & addresses.size() > 0){
			Address address = addresses.get(0);
			String addressText = String.format("%s, %s, %s", address.getMaxAddressLineIndex() > 0 ?
					address.getAddressLine(0) : "", 
					address.getLocality(),
					address.getCountryName());

			textAddr.setText(addressText);
		}
	}

	public boolean canGetLocation(){
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
