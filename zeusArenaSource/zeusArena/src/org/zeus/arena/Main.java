package org.zeus.arena;


import quake.two.android.Quake2;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		Button startEngine1 = (Button)findViewById(R.id.startEngine1);
		startEngine1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),Game.class);
				startActivity(intent);
			}
		});
		
		Button startEngine2 = (Button)findViewById(R.id.startEngine2);
		startEngine2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),Quake2.class);
				startActivity(intent);
			}
		});
	}
}
