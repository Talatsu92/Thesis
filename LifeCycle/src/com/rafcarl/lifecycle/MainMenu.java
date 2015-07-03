package com.rafcarl.lifecycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends Activity {
	public static final String preferenceFile = "com.rafcarl.lifecycle.flags";
	SharedPreferences sharedPref = null;
	SharedPreferences.Editor editor = null;
	public static final String LOG = "MainMenu";
	
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
	
//	public void testDialog(View v){
//		final AlertDialog.Builder builder= new AlertDialog.Builder(this);
//		builder.setTitle("Test Dialog")
//			   .setMessage("First Message")
//			   .setNegativeButton("Close", new DialogInterface.OnClickListener() {
//				   
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						
//					}
//			   	})
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						builder.setMessage("Second Message");			
//					}
//				});
//		
//		AlertDialog dialog = builder.create();
//		
//		dialog.show();
//	}
	
//	@Override
//	public void onDialogMessage(String message) {
//		Toast.makeText(this, message,  Toast.LENGTH_SHORT).show();
//	}
	
//	public void showDialog(View v){
//		FragmentManager fm = getFragmentManager();
//		Dialog d = new Dialog();
//		d.show(fm, "Dialog");
//	}
	

	public void goToUser(View v){
		Intent intent = new Intent(this, UserMenu.class);
		startActivity(intent);
	}

	public void goToContacts(View v){
		Intent intent = new Intent(this, ContactsMenu.class);
		startActivity(intent);
	}

	public void goToHelp(View v){
		Intent intent = new Intent(this, HelpMenu.class);
		startActivity(intent);
	}

	public void goToTest(View v){
		Intent intent = new Intent(this, SensorTest.class);
		startActivity(intent);
	}
	
	public void goToSMS(View v){
		Intent intent = new Intent(this, SMSTest.class);
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pre-Monitor");
		builder.setMessage(R.string.preMonitor);
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

				countDown();
			}
		});		

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	//Displays AlertDialog counting down from one minute
	public void countDown(){
		final AlertDialog timerDialog = new AlertDialog.Builder(this).create();
		timerDialog.setTitle("Secure the device");
		timerDialog.setMessage("0:45");
		timerDialog.setCanceledOnTouchOutside(false);
		timerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();	
			}
		});

		CountDownTimer timer = new CountDownTimer(5000, 1000){
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
					Context context = getWindow().getContext();
					
					monitor = new Monitor(accelerometer, gyroscope, mSensorManager, locationManager, connectivityManager, MainMenu.this, context);
					Log.i(LOG, "Monitor() called");
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
