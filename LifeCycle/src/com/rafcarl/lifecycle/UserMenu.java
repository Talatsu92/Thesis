package com.rafcarl.lifecycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UserMenu extends Activity {

	SharedPreferences sharedPref;

	TextView name;
	TextView age;
	Spinner blood;
	Spinner anti;
	TextView height;
	Spinner heightspin;
	TextView weight;
	TextView meds;
	TextView conds;
	TextView allergs;
	
	public static final String preferenceFile = "com.rafcarl.lifecycle.flags";
	public static final String Name = "nameKey"; 
	public static final String Age = "ageKey"; 
	public static final String Blood = "bloodKey"; 
	public static final String Anti = "antiKey";
	public static final String Height = "heightKey"; 
	public static final String HeightSpin = "heightspinKey"; 
	public static final String Weight = "weightKey";
	public static final String Meds = "medKey";
	public static final String Conds = "condtKey";
	public static final String Allergs = "allergKey";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_menu);

		name = (TextView) findViewById(R.id.user_name_i);
		age = (TextView) findViewById(R.id.user_age_i);
		blood = (Spinner) findViewById(R.id.user_blood_s);
		anti = (Spinner) findViewById(R.id.user_blood_a);
		height = (TextView) findViewById(R.id.user_height_i);
		heightspin = (Spinner) findViewById(R.id.user_height_s);
		weight = (TextView) findViewById(R.id.user_weight_i);
		meds = (TextView) findViewById(R.id.user_multi1_i);
		conds = (TextView) findViewById(R.id.user_multi2_i);
		allergs = (TextView) findViewById(R.id.user_multi3_i);
		
		sharedPref = getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
		
		if(sharedPref.contains(Name)){name.setText(sharedPref.getString(Name, ""));}
		if(sharedPref.contains(Age)){age.setText(sharedPref.getString(Age, ""));}
		if(sharedPref.contains(Blood)){blood.setSelection(sharedPref.getInt(Blood, 0));}
		if(sharedPref.contains(Anti)){anti.setSelection(sharedPref.getInt(Anti, 0));}
		if(sharedPref.contains(Height)){height.setText(sharedPref.getString(Height, ""));}
		if(sharedPref.contains(HeightSpin)){heightspin.setSelection(sharedPref.getInt(HeightSpin, 0));}
		if(sharedPref.contains(Weight)){weight.setText(sharedPref.getString(Weight, ""));}
		if(sharedPref.contains(Meds)){meds.setText(sharedPref.getString(Meds, ""));}
		if(sharedPref.contains(Conds)){conds.setText(sharedPref.getString(Conds, ""));}
		if(sharedPref.contains(Allergs)){allergs.setText(sharedPref.getString(Allergs, ""));}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_menu, menu);

		getActionBar().setDisplayHomeAsUpEnabled(true);

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

	private void clearForm(ViewGroup group)
	{       
		for (int i = 0, count = group.getChildCount(); i < count; ++i) {
			View view = group.getChildAt(i);
			if (view instanceof EditText){
				((EditText)view).setText("");
			}
			if (view instanceof Spinner){
				((Spinner)view).setSelection(0);
			}

			if(view instanceof ViewGroup && (((ViewGroup)view).getChildCount() > 0))
				clearForm((ViewGroup)view);
		}
	}

	public void saveUserInfo(View view){
		String n = name.getText().toString();
		String a = age.getText().toString();
		int b = blood.getSelectedItemPosition();
		int an = anti.getSelectedItemPosition();
		String h = height.getText().toString();
		int hs = heightspin.getSelectedItemPosition();
		String w = weight.getText().toString();
		String m = meds.getText().toString();
		String c = conds.getText().toString();
		String al = allergs.getText().toString();

		Editor editor = sharedPref.edit();
		editor.putString(Name, n);
		editor.putString(Age, a);
		editor.putInt(Blood, b);
		editor.putInt(Anti, an);
		editor.putString(Height, h);
		editor.putInt(HeightSpin, hs);
		editor.putString(Weight, w);
		editor.putString(Meds, m);
		editor.putString(Conds, c);
		editor.putString(Allergs, al);

		editor.commit();
		Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();

		sharedPref = getSharedPreferences("com.rafcarl.lifecycle.flags", Context.MODE_PRIVATE);
		boolean bool = sharedPref.getBoolean(Flags.FIRST_RUN, true);
		if(bool == true){
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("First Use");
			builder.setMessage(R.string.redirectContacts);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					Intent intent = new Intent(getApplicationContext(), ContactsMenu.class);	
					startActivity(intent);
				}
			});

			AlertDialog dialog = builder.create();	
			dialog.show();
		}

	}

	public void clearUserInfo(View view){
		ViewGroup group = (ViewGroup) findViewById(R.id.scrollView1);
		clearForm(group);
		Editor editor = sharedPref.edit();
		editor.clear();
		editor.commit();
		Toast.makeText(this, "Cleared", Toast.LENGTH_LONG).show();
	}
}
