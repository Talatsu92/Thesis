package com.rafcarl.lifecycle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
	}
	
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
}
