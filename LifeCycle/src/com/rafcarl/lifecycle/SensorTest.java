package com.rafcarl.lifecycle;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SensorTest extends Activity implements SensorEventListener{
	private SensorManager mSensorManager;
	TextView accelInfo, acceleration = null;
	TextView gyroInfo, rotation = null;
	Sensor accelerometer;
	Sensor gyroscope;
	Button accel;
	static final String LOG = "SensorTest";
	
	//	Accelerometer
	public float[] gravity;
	public float[] linear_acceleration;
	float alpha = 0.9f;

	//	float timeConstant = 0.18f;
	//	float dt = 0;
	//	float timestamp = System.nanoTime();
	//	float timestampOld = System.nanoTime();
	//	private int count = 0;

	//	Gyroscope
	static final float EPSILON = 0.000000001f;
	static final float NS2S = 1.0f / 1000000000.0f;
	float[] deltaRotationVector = new float[4];
	float timestamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_test);

		Log.i(LOG, "onCreate() called");
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		if(accelerometer != null && gyroscope != null){
			Toast.makeText(this, "Sensors detected", Toast.LENGTH_SHORT).show();
			acceleration = (TextView) findViewById(R.id.accelValues);
			rotation = (TextView) findViewById(R.id.gyroValues);

			Log.i(LOG, "if(accelerometer != null && gyroscope != null) -> true");
			accel = (Button) findViewById(R.id.accelStart);
			accel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
//					Monitor monitor = new Monitor(accelerometer, gyroscope, mSensorManager);
					Log.i(LOG, "accel onClick() called");
//					accelStart(v, monitor);
				}
			});
			//			gravity = new float[] {0, 0, 0};
			//			linear_acceleration = new float[]{0, 0, 0};
		}
		else{
			Toast.makeText(this, "One of the sensors does not exist", Toast.LENGTH_SHORT).show();
		}
	}

	public void accelStart(View v, Monitor m){
		Log.i(LOG, "accelStart() called");
		float minDelay, resolution, range, power;
		String vendor;

		accelInfo = (TextView) findViewById(R.id.accelInfo);
		vendor = accelerometer.getVendor();
		minDelay = accelerometer.getMinDelay();
		resolution = accelerometer.getResolution();
		range = accelerometer.getMaximumRange();
		power = accelerometer.getPower();
		
		accelInfo.setText("Vendor: " + vendor 
				      + "\nMinimum Delay: " + minDelay 
				      + "\nResolution: " + resolution 
				      + "\nRange: " + range 
				      + "\nPower: " + power);
		m.startMonitoring();
		//mSensorManager.registerListener(this, accelerometer, 5000);
	}

	public void gyroStart(View v){		
		float minDelay, resolution, range, power;
		String vendor;

		gyroInfo = (TextView) findViewById(R.id.gyroInfo);
		vendor = gyroscope.getVendor();
		minDelay = gyroscope.getMinDelay();
		resolution = gyroscope.getResolution();
		range = gyroscope.getMaximumRange();
		power = gyroscope.getPower();

		gyroInfo.setText("Vendor: " + vendor 
				+ "\nMinimum Delay: " + minDelay 
				+ "\nResolution: " + resolution 
				+ "\nRange: " + range 
				+ "\nPower: " + power);

		mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void sensorStop(View v){
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();

		switch (type) {
		case Sensor.TYPE_ACCELEROMETER:
			double sumVector = Math.sqrt(Math.pow(event.values[0], 2) 
					+ Math.pow(event.values[1], 2) 
					+ Math.pow(event.values[2], 2))
					/ SensorManager.GRAVITY_EARTH;
			if(sumVector <= 0.6f){
				mSensorManager.unregisterListener(this, accelerometer);
				Toast.makeText(this, "Threshold broken " + sumVector, Toast.LENGTH_LONG).show();				
			}
			else{
				acceleration.setText("Accelerometer"
						+ "\nX: " + event.values[0] 
								+ "\nY: " + event.values[1]
										+ "\nZ: " + event.values[2]
												+ "\nSV: " + sumVector);
			}

			break;
		case Sensor.TYPE_GYROSCOPE:
			//				if(timestamp != 0){
			//					final float dT = (event.timestamp - timestamp) * NS2S;
			//					
			//					float axisX = event.values[0];
			//					float axisY = event.values[1];
			//					float axisZ = event.values[2];
			//					
			//					float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
			//					
			//					if(omegaMagnitude > EPSILON){
			//						axisX /= omegaMagnitude;
			//						axisY /= omegaMagnitude;
			//						axisZ /= omegaMagnitude;
			//					}
			//				
			//					double thetaOverTwo = omegaMagnitude * dT / 2.0f;
			//					float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
			//					float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
			//					deltaRotationVector[0] = sinThetaOverTwo * axisX;
			//					deltaRotationVector[1] = sinThetaOverTwo * axisY;
			//					deltaRotationVector[2] = sinThetaOverTwo * axisZ;
			//					deltaRotationVector[3] = cosThetaOverTwo;
			//				}

			rotation.setText("\nGyroscope"
					+ "\nX: " + event.values[0] 
							+ "\nY: "  + event.values[1]
									+ "\nZ: "  + event.values[2]);

			if(Math.abs(event.values[0]) >= 8.0f){
				mSensorManager.unregisterListener(this, gyroscope);
				Toast.makeText(this, "Gyroscope rotation X: " + event.values[0], Toast.LENGTH_SHORT).show();
			}
			else if(Math.abs(event.values[1]) >= 8.0f){
				mSensorManager.unregisterListener(this, gyroscope);
				Toast.makeText(this, "Gyroscope rotation Y: " + event.values[1], Toast.LENGTH_SHORT).show();
			}
			else if(Math.abs(event.values[2]) >= 8.0f){
				mSensorManager.unregisterListener(this, gyroscope);
				Toast.makeText(this, "Gyroscope rotation Z: " + event.values[2], Toast.LENGTH_SHORT).show();
			}

			break;

		default:
			break;
		}
		//		timestamp = System.nanoTime();

		// Find the sample period
		//		dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f));
		//		count++;

		// Set dynamic alpha
		//		alpha = timeConstant / (timeConstant + dt);

		// Acceleration due to gravity (Low Pass Filter)
		//		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		//		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		//		gravity[2] = alpha * gravity[1] + (1 - alpha) * event.values[2];

		// Linear acceleration = raw acceleration data - acceleration due to gravity (High Pass Filter)
		//		linear_acceleration[0] = event.values[0] - gravity[0];
		//		linear_acceleration[1] = event.values[1] - gravity[1];
		//		linear_acceleration[2] = event.values[2] - gravity[2];


	}
	@Override
	protected void onPause() {
		super.onPause();
		//		mSensorManager.unregisterListener(this);
	}



	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.accel_test, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
