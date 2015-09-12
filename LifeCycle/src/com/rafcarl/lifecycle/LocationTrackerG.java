package com.rafcarl.lifecycle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class LocationTrackerG extends Activity 
							  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, Runnable{
	
	GoogleApiClient googleApiClient = null;
	LocationRequest locationRequest = null;
	private double latitude;
	private double longitude;
	
//	String latitude = null;
//	String longitude = null;
	String address = null;

	TextView textView = null;
	
	View customLayout = null;
	
	LayoutInflater inflater = null;
	Context context = null;

//	public LocationTrackerG(Context context, TextView tv){
	public LocationTrackerG(Context context, String addr){
//		textView = tv;

		this.address = addr;
		this.context = context;
		
		
		googleApiClient = new GoogleApiClient.Builder(context)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(LocationServices.API)
			.build();
		
		locationRequest = new LocationRequest();
		locationRequest.setInterval(10000);
		locationRequest.setFastestInterval(5000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);	
		
		googleApiClient.connect();
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//		textView.setText("Location: Searching...");
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		if(location != null){
			Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());
			List<Address> result = null;
			Address single;
			
			try {
				setLatitude(location.getLatitude());
				setLongitude(location.getLongitude());
				result = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				single = result.get(0);
				this.address = single.getAddressLine(0) + ", " + single.getAddressLine(1) + " " + single.getPostalCode() + ", " + single.getCountryName() + "\nLatitude: " +location.getLatitude() + "\nLongitude: " + location.getLongitude();
				L.m(this.address);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			
//			latitude = String.valueOf(location.getLatitude());
//			longitude = String.valueOf(location.getLongitude());
//			
//			L.m("Latitude: " + latitude + "\nLongitude: " + longitude);
//			
//			textView.setText("Location: Found");

			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			googleApiClient.disconnect();
			
//			L.m("removeLocationUpdates() called, googleApiClient disconnected");			
		}
	}
	
	public String getAddress(){
		return this.address;
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		googleApiClient.disconnect();
		Toast.makeText(context, "Connection suspended", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		googleApiClient.disconnect();
		Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}
							  