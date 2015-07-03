package com.rafcarl.lifecycle;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class L {

	public L() {
		// TODO Auto-generated constructor stub
	}
	
	public static void m(String message){
		Log.i("LifeCycle", message);
	}
	
	public static void s(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

}
