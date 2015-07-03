package com.rafcarl.lifecycle;

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
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class LocationTrackerG extends Activity 
							  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	
	GoogleApiClient googleApiClient = null;
	LocationRequest locationRequest = null;
	
	String latitude = null;
	String longitude = null;

	TextView textView = null;
	
	View customLayout = null;
	
	LayoutInflater inflater = null;
	Context context = null;

	public LocationTrackerG(Context context, TextView tv){
		this.context = context;
		textView = tv;
		
		googleApiClient = new GoogleApiClient.Builder(this)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(LocationServices.API)
			.build();
		
		locationRequest = new LocationRequest();
		locationRequest.setInterval(10000);
		locationRequest.setFastestInterval(5000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);	
	}
	
	public void start(){
		googleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
		textView.setText("Location: Searching...");
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		if(location != null){
			latitude = String.valueOf(location.getLatitude());
			longitude = String.valueOf(location.getLongitude());
			
			L.m("Latitude: " + latitude + "\nLongitude: " + longitude);
			
			textView.setText("Location: Found");

			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			googleApiClient.disconnect();
			
			L.m("removeLocationUpdates() called, googleApiClient disconnected");			
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "Connection suspended", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show();
	}
	
}
							  