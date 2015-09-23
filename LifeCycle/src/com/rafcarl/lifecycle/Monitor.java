package com.rafcarl.lifecycle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Monitor extends Activity implements SensorEventListener{
	static final String LOG = "Monitor";
	static int SAMPLE_SIZE ;//(int) 1200000/SensorManager.SENSOR_DELAY_FASTEST;
	
	Activity activity = null;
	Context context = null;
	SensorManager mSensorManager = null;
	AudioManager am = null;
	Sensor accelerometer = null;
	Sensor gyroscope = null;

	public static short accelCount;	
	public static float accelValues[] = null;

	public static int config;	
	public static boolean oneSecondMonitor;
	public static boolean rotation;
	public static boolean impact;
	
	AlertDialog.Builder builder = null;
	AlertDialog alertDialog = null;
	AlertDialog timerDialog = null;
	
	View view = null;
	LayoutInflater inflater = null;
	
	String latitude;
	String longitude;
	String address;

	public Monitor(Sensor a, Sensor g, SensorManager m, Activity act, Context c) {		
		Log.i(LOG, "constructor called");
		mSensorManager = m;
		accelerometer = a;
		gyroscope = g;
		activity = act;
		context = c;
		am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		SAMPLE_SIZE = (int)(1.5/(accelerometer.getMinDelay() * Math.pow(10, -6)));
		accelValues = new float[SAMPLE_SIZE];
		
		inflater = LayoutInflater.from(context);
		
		view = inflater.inflate(R.layout.dialog_accident, null, false);
		
		builder = new AlertDialog.Builder(context);
		L.m("AlertDialog.Builder() called");
		
		builder.setTitle("Accident detected")
			   .setView(view)
			   .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		
		alertDialog = builder.create();
		
		L.m(String.valueOf(accelerometer.getMinDelay()));
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
				} else if(oneSecondMonitor == true){
					if(accelCount < SAMPLE_SIZE){
						accelValues[accelCount++] = sumVector;
					} else {
						mSensorManager.unregisterListener(this);
						impact = checkImpact();
						if(impact == true && rotation == true){
							Log.i(LOG, "Accident detected");

							if(isExternalStorageWritable()){
								//Formats the date and time 
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
								String dateTime = dateFormat.format(Calendar.getInstance().getTime());
								
								writeToStorage("LifeCycle - " + dateTime);
							}
							
							ImageButton startButton = (ImageButton) activity.findViewById(R.id.StartButton);
							TextView tv = (TextView) activity.findViewById(R.id.StartText);
	
							startButton.setImageResource(R.drawable.play_icon);
							tv.setText(R.string.start);
	
							MainMenu.monitoring = false;
							
							countDown(this.address);
						} else{
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
	
	//Displays AlertDialog counting down from one minute
		public void countDown(String address){
			final LocationTrackerG locationTracker = new LocationTrackerG(context, address);
			
			final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
			
			int result = am.requestAudioFocus(new OnAudioFocusChangeListener() {
				
				@Override
				public void onAudioFocusChange(int focusChange) {
					am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				}
			}, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			
			final CountDownTimer timer = new CountDownTimer(15000, 1000){
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
					if(locationTracker.hasLocation()){
						Message message = new Message(locationTracker.getLatitude(), locationTracker.getLongitude());
						timerDialog.dismiss();
						
						//send text message
						message.sendMessage(context);
						
						am.abandonAudioFocus(null);
						mediaPlayer.stop();
						mediaPlayer.release();
					} else{
						Toast.makeText(context, "Location not found", Toast.LENGTH_LONG).show();
					}
				} 
			};
			
			timerDialog = new AlertDialog.Builder(context).create();
			timerDialog.setTitle("Accident detected! Sending messages in:");
			timerDialog.setMessage("00:45");
			timerDialog.setCanceledOnTouchOutside(false);
			timerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();	
					am.abandonAudioFocus(null);
					mediaPlayer.stop();
					mediaPlayer.release();
					timer.cancel();
					locationTracker.disconnect();
				}
			});
			
			timerDialog.show();
			if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
				mediaPlayer.start();
			}
			
			timer.start();
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
	
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public void writeToStorage(String filename){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < accelValues.length; i++){
			sb.append((i + 1) + ", " + accelValues[i] + "\r\n");
		}
		
		try {
			File file = new File(Environment.getExternalStorageDirectory() + "/Documents", filename + ".csv");
			file.createNewFile();
			
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(sb.toString().getBytes()); 
			fos.close();
			
		} catch(NullPointerException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
