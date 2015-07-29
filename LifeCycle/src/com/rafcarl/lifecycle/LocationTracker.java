package com.rafcarl.lifecycle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
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

    // The minimum time between updates in milliseconds
	private static final long time = 1000 * 60 * 1;	//1 minute
    // The minimum distance to change Updates in meters
	private static final long distance = 10;	//10 meters
	
	String streetAddress;

	public LocationTracker(LocationManager locationManager, ConnectivityManager connectivityManager) {
		Log.i(LOG, "LocationTracker constructor enter");
		this.locationManager = locationManager;
		this.connectivityManager = connectivityManager;

		this.gpsLocation = null;
		this.networkLocation = null;
		this.mLocation = null;
	}
	
	public Location getLocation(){
		Log.i(LOG, "getLocation() enter");
		boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if(isNetworkEnabled){
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, this);
			networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Log.i(LOG, "requestLocationUpdates(NetworkProvider)" + mLocation);
		}
		if(isGPSEnabled){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, this);
			gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Log.i(LOG, "requestLocationUpdates(GPS Provider)" + mLocation);
		}
		mLocation = checkAccuracy(networkLocation, gpsLocation);
		if(mLocation != null){
			latitude = mLocation.getLatitude();
			longitude = mLocation.getLongitude();
		}
		return mLocation;
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

		mLocation = checkAccuracy(networkLocation, gpsLocation);
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

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
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

	public String displayAddress(){
		String addressText = "";
		latitude = getLatitude();
		longitude = getLongitude();

		Geocoder geocoder = new Geocoder(context);
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(addresses != null & addresses.size() > 0){
			Address address = addresses.get(0);
			addressText = String.format("%s, %s, %s", address.getMaxAddressLineIndex() > 0 ?
					address.getAddressLine(0) : "", 
					address.getLocality(),
					address.getCountryName());
		}
		return addressText;
	}

	public boolean canGetLocation(){
		Log.i(LOG, "canGetLocation() enter");
		NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		return (mWifi.isConnected() || mMobile.isConnected()) ? true : false;
	}

	public double getLatitude() {
		return (mLocation != null) ? mLocation.getLatitude(): latitude;
	}

	public double getLongitude() {
		return (mLocation != null) ? mLocation.getLongitude(): longitude;
	}
	
	public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is Disabled");
  
        // Setting Dialog Message
        alertDialog.setMessage("Do you want to go to Settings menu?");
  
        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);
  
        // On pressing Settings button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
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
