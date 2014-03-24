package org.zeus.arena;

import org.zeus.arena.controls.Controls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class Menu extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		Button controlSetup = (Button)findViewById(R.id.ControlSetup);
		controlSetup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),Controls.class);
				startActivity(intent);
			}
		});
		Button keyboard = (Button)findViewById(R.id.showKeyboard);
		keyboard.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
				Persistence.getPersistence().getGame().showKeyboard();
			}
		});
		Button startupOptions = (Button)findViewById(R.id.startupOptions);
		startupOptions.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),StartupOptions.class);
				startActivity(intent);
			}
		});
		Button help = (Button)findViewById(R.id.helpbutton);
		help.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),Help.class);
				startActivity(intent);
			}
		});
	}

}
