package com.rafcarl.lifecycle;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.ContextThemeWrapper;

public class GpsLocationTracker extends Service implements LocationListener{
	private Context mContext;
	private boolean isGpsEnabled = false;
	private boolean isNetworkEnabled = false;
	private boolean canGetLocation = false;
	private Location mLocation;
	private double mLatitude;
	private double mLongitude;
	private String provider = LocationManager.GPS_PROVIDER;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;
	private static final long MIN_TIME_FOR_UPDATE = 60000;
	private LocationManager mLocationManager;

	public GpsLocationTracker(Context mContext) {
		this.mContext = mContext;
		getLocation();
	}

	public Location getLocation() {

		try {

			mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			/*getting status of the gps*/
			isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			/*getting status of network provider*/
			isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGpsEnabled && !isNetworkEnabled) {
				/*no location provider enabled*/
			}
			else{
				this.canGetLocation = true;
				/*getting location from network provider*/
				if (isNetworkEnabled) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
					if (mLocationManager != null) {
						mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (mLocation != null) {
							mLatitude = mLocation.getLatitude();
							mLongitude = mLocation.getLongitude();
						}
					}
					/*if gps is enabled then get location using gps*/
					if (isGpsEnabled) {
						if (mLocation == null) {
							mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
							if (mLocationManager != null) {
								mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
								if (mLocation != null) {
									mLatitude = mLocation.getLatitude();
									mLongitude = mLocation.getLongitude();
								}
							}
						}

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mLocation;
	}

	public void stopUsingGps() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(GpsLocationTracker.this);
		}
	}

	public double getLatitude() {
		if (mLocation != null){
			mLatitude = mLocation.getLatitude();
		}
		return mLatitude;
	}

	public double getLongitude() {
		if (mLocation != null){
			mLongitude = mLocation.getLongitude();
		}
		return mLongitude;
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	public void showSettingsAlert() {
		AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AppTheme));
		mAlertDialog.setTitle("Gps Disabled");
		mAlertDialog.setMessage("gps is not enabled . do you want to enable ?");
		mAlertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(mIntent);
			}
		});

		mAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});

		final AlertDialog mcreateDialog = mAlertDialog.create();
		mcreateDialog.show();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
