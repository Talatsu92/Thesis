package com.rafcarl.lifecycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends Activity {
	public static final String preferenceFile = "com.rafcarl.lifecycle.prefs";
	SharedPreferences sharedPref = null;
	SharedPreferences.Editor editor = null;
	public static final String LOG = "MainMenu";
	public static final String Blood = "bloodKey"; 
	
	//System Managers
	SensorManager mSensorManager = null;
	LocationManager locationManager = null;
	ConnectivityManager connectivityManager = null;
	
	//Sensor variables
	Sensor accelerometer = null;
	Sensor gyroscope = null;
	
	Monitor monitor = null;
	
	static boolean monitoring;
	
	AlertDialog alertDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main_menu);
		
		monitoring = false;
		
		//System Services
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		sharedPref = getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
		
		if(sharedPref.getBoolean(Flags.FIRST_RUN, true)){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("First Use");
			builder.setMessage(R.string.redirectUser);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {					
					Intent intent = new Intent(getApplicationContext(), UserMenu.class);
					startActivity(intent);
				}
			});

			AlertDialog dialog = builder.create();	
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		ImageButton startButton = (ImageButton) findViewById(R.id.StartButton);
		startButton.setImageResource(R.drawable.play_icon);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(monitoring == false){
					preMonitorDialog(v);
				}
				else{
					pauseMonitorDialog();
				}
			}
		});
				
	}	

	public void goToUser(View v){
		Intent intent = new Intent(this, UserMenu.class);
		startActivity(intent);
	}

	public void goToContacts(View v){
		Intent intent = new Intent(this, ContactsMenu.class);
		startActivity(intent);
	}
	
	public void openAboutDialog(View v){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("LifeCycle");
		builder.setMessage(R.string.aboutDialog);
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	//Prompts User to secure device within one minute
	public void preMonitorDialog(View v){
		DBHelper.dbHelper = new DBHelper(this);
		DBHelper.db = DBHelper.dbHelper.getReadableDatabase();
		Cursor cursor = DBHelper.db.rawQuery("SELECT * FROM LifeCycleTable", null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pre-Monitor");
		
		LocationTrackerG lg = new LocationTrackerG(this);

		if(cursor.moveToFirst() == false){
			builder.setMessage("You have not set any emergency contacts.");
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setPositiveButton("Contacts Menu", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(getApplicationContext(), ContactsMenu.class);
					startActivity(intent);
				}
			});
		} else if(!sharedPref.contains(Message.Blood) || !sharedPref.contains(Message.Anti)){
			builder.setTitle("Set Blood Type");
			builder.setMessage("This app requires the user's blood type.");
			builder.setPositiveButton("User Menu", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(getApplicationContext(), UserMenu.class);
					startActivity(intent);
				}
			});
			
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		} else if(!lg.ifSettingsChecked()){
			builder.setTitle("Set Location Sources");
			builder.setMessage("This app requires the use of GPS and wireless networks enabled.");
			builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
				}
			});

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
		} else{
			builder.setMessage(R.string.preMonitor);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

					countDown();
				}
			});
		}

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	//Displays AlertDialog counting down from 45 seconds
	public void countDown(){
		final AlertDialog timerDialog = new AlertDialog.Builder(this).create();

		final CountDownTimer timer = new CountDownTimer(5000, 1000){
			@Override
			public void onTick(long millisUntilFinished) {
				if((millisUntilFinished/1000) == 45){
					timerDialog.setMessage("00:45");
				}
				else if((millisUntilFinished/1000) < 10){
					timerDialog.setMessage("00:0" + (millisUntilFinished/1000));
				}
				else{
					timerDialog.setMessage("00:" + (millisUntilFinished/1000));
				}
			}

			@Override
			public void onFinish() {
				if(monitor == null){
					monitor = new Monitor(accelerometer, gyroscope, mSensorManager,MainMenu.this, getWindow().getContext());
				}
				
				ImageButton startButton = (ImageButton) findViewById(R.id.StartButton);
				TextView tv = (TextView) findViewById(R.id.StartText);
				
				startButton.setImageResource(R.drawable.pause_icon);				
				tv.setText(R.string.monitor);
				
				monitoring = true;				
				monitor.startMonitoring();
				
				timerDialog.dismiss();
			}
		};
		
		timerDialog.setTitle("Secure the device");
		timerDialog.setMessage("0:45");
		timerDialog.setCancelable(false);
		timerDialog.setCanceledOnTouchOutside(false);
		timerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				timer.cancel();
				dialog.cancel();	
			}
		});

		timerDialog.show();
		timer.start();
	}
	
	public void pauseMonitorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pause monitoring?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				monitoring = false;
				monitor.pauseMonitoring();
				
				ImageButton startButton = (ImageButton) findViewById(R.id.StartButton);
				startButton.setImageResource(R.drawable.play_icon);
				
				TextView tv = (TextView) findViewById(R.id.StartText);
				tv.setText(R.string.start);
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
