package com.rafcarl.lifecycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Monitor extends Activity implements SensorEventListener{
	static final String LOG = "Monitor";
	static final int SAMPLE_SIZE = 60;//(int) 1200000/SensorManager.SENSOR_DELAY_FASTEST;

	Activity activity;
	Context context;
	SensorManager mSensorManager;
	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Sensor accelerometer;
	Sensor gyroscope;

	public static short accelCount;	
	public static float accelValues[] = new float[SAMPLE_SIZE];

	public static int config;	
	public static boolean oneSecondMonitor;
	public static boolean rotation;
	public static boolean impact;
	
	AlertDialog.Builder builder = null;
	AlertDialog alertDialog = null;
	AlertDialog timerDialog = null;
	
	View view = null;
	LayoutInflater inflater = null;

	public Monitor(Sensor a, Sensor g, SensorManager m, LocationManager lM, ConnectivityManager cM, Activity act, Context c) {		
		Log.i(LOG, "constructor called");
		mSensorManager = m;
		accelerometer = a;
		gyroscope = g;
		locationManager = lM;
		connectivityManager = cM;
		activity = act;
		context = c;
		
		inflater = LayoutInflater.from(context);
		
		view = inflater.inflate(R.layout.dialog_accident, null, false);
		
		builder = new AlertDialog.Builder(context);
		L.m("AlertDialog.Builder() called");
		
		builder.setTitle("Accident detected")
			   .setView(view)
			   .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
		
		alertDialog = builder.create();
		
	}
	
	//Displays AlertDialog counting down from one minute
	public void countDown(){
		timerDialog = new AlertDialog.Builder(context).create();
		timerDialog.setTitle("Sending message(s) in:");
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
				
				
				timerDialog.dismiss();
			} 
		};

		timerDialog.show();
		timer.start();
	}

	public int obtainConfig(){
		//0 - No sensors
		//1 - Only Accelerometer
		//2 - Accelerometer & Gyroscope
		Log.i(LOG, "obtainConfig() called");

		int result = 0;
			
		if(accelerometer != null){
			if(gyroscope != null){
				result = 2;
			}
			else
				result = 1;
		}

		Log.i(LOG, result + " config obtained");
		return result;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();

		switch (type) {
		case Sensor.TYPE_ACCELEROMETER:
			float sumVector = (float) Math.sqrt(Math.pow(event.values[0], 2) 
					+ Math.pow(event.values[1], 2)  
					+ Math.pow(event.values[2], 2))
					/ SensorManager.GRAVITY_EARTH;
			if(oneSecondMonitor == false && sumVector <= 0.6){
				oneSecondMonitor = true;
				mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
			}
			else if(oneSecondMonitor == true){
				if(accelCount < SAMPLE_SIZE){
					accelValues[accelCount++] = sumVector;
				}
				else {
					mSensorManager.unregisterListener(this);
					impact = checkImpact();
					if(impact == true && rotation == true){
						Log.i(LOG, "Accident detected");
						ImageButton startButton = (ImageButton) activity.findViewById(R.id.StartButton);
						TextView tv = (TextView) activity.findViewById(R.id.StartText);

						startButton.setImageResource(R.drawable.play_icon);
						tv.setText(R.string.start);

						MainMenu.monitoring = false;

						countDown(); 
						
						alertDialog.show();
						
//						AccidentDialog accidentDialog = new AccidentDialog();
//						accidentDialog.show(activity.getFragmentManager(), "Accident Dialog");
			
//						Message message = new Message(locationManager, connectivityManager);
//						Log.i(LOG, "instantiate new Message object");
//						if(message.getLocation() == 1){
//							Log.i(LOG, "message.getLocation()");
//							TextView one = (TextView) findViewById(R.id.accident_locate);
//							one.setText("Obtain user location " + "\u2713");
//							message.convertToDMS();
//							message.getContacts();
//							message.promptUser(accidentDialog);
//						}
//						else{
//							Toast.makeText(getApplicationContext(), "Unable to retrieve location", Toast.LENGTH_LONG).show();
//						}
						
						//locate user
						//prepare messages
						//prompt user
						//send messages
					}
					else{
						oneSecondMonitor = false;
						impact = false;
						rotation = false;
						accelCount = 0;
						mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
					}
				}
			}

			break;

		case Sensor.TYPE_GYROSCOPE:
			if(Math.abs(event.values[0]) >= 4.0f || Math.abs(event.values[1]) >= 4.0f || Math.abs(event.values[2]) >= 4.0f){
				rotation = true;
				mSensorManager.unregisterListener(this, gyroscope);
			}

			break;

		default:
			break;
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void startMonitoring(){
		Log.i(LOG, "startMonitoring() enter");
		oneSecondMonitor = false;
		rotation = false;
		impact = false;
		accelCount = 0;

		config = obtainConfig();

		Log.i(LOG, "obtainConfig() exited");

		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void pauseMonitoring(){
		mSensorManager.unregisterListener(this);
	}

	public boolean checkImpact(){
		boolean result = false;
		int i;
		float min;
		float max;

		for(i = 1, min = max = accelValues[0]; i < SAMPLE_SIZE; i++){
			if(accelValues[i] > max){
				max = accelValues[i];
			}
			if(accelValues[i] < min){
				min = accelValues[i];
			}
		}
		if((min <= 0.2f) && (max >= 2.2f )){
			result = true;
		}

		return result;
	}

	

}
