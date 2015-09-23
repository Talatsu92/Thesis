package com.rafcarl.lifecycle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.http.util.LangUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class LocationTrackerG extends Activity 
							  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, Runnable{
	
	GoogleApiClient googleApiClient = null;
	LocationRequest locationRequest = null;
	private double latitude = 0.0;
	private double longitude = 0.0;
	
	String address = null;
	TextView textView = null;
	View customLayout = null;
	
	LayoutInflater inflater = null;
	Context context = null;

	public LocationTrackerG(Context context){
		this.context = context;
	}
	
	public LocationTrackerG(Context context, String addr){
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
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
	}
	
	@Override
	public void onLocationChanged(Location location) {		
		if(location != null){
			Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());
			List<Address> result = null;
			Address single;
			
			try {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				result = geocoder.getFromLocation(latitude, longitude, 1);
				single = result.get(0);
				this.address = single.getAddressLine(0) + ", " + single.getAddressLine(1) + " " + single.getPostalCode() + ", " + single.getCountryName() + "\nLatitude: " +location.getLatitude() + "\nLongitude: " + location.getLongitude();
				L.m(this.address);
			} catch (IOException e) {
				e.printStackTrace();
			}

			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			googleApiClient.disconnect();			
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
	 
	}

	public boolean ifSettingsChecked(){
		LocationManager lM = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
		boolean isNetworkEnabled = lM.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean isGpsEnabled = lM.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(isNetworkEnabled == true && isGpsEnabled == true){
			return true;
		}
		
		return false;
	}
	
	public boolean hasLocation(){
		boolean result = false;
		
		if(latitude != 0 && longitude != 0){
			result = true;
		}
		
		return result;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}	
	
	public void disconnect(){
		googleApiClient.disconnect();
	}
}
							  