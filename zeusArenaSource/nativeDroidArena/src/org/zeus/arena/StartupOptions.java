package org.zeus.arena;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class StartupOptions extends Activity{
	
	private CheckBox sound;
	private CheckBox lightMaps;
	private CheckBox fps;
	private CheckBox pureServers;
	private CheckBox protocol;
	private Game game;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startupoptions);
		sound = (CheckBox) findViewById(R.id.soundCheckBox);
		lightMaps = (CheckBox) findViewById(R.id.lightmapsCheckBox);
		fps = (CheckBox) findViewById(R.id.fpsCheckBox);
		pureServers = (CheckBox) findViewById(R.id.pureServers);
		protocol = (CheckBox) findViewById(R.id.protocolBox);
		checkPreferences();
		game = Persistence.getPersistence().getGame();
		sound.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (sound.isChecked()){
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("Sound", true);
				}
				else{
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("Sound", false);
				}
			}
		});
		lightMaps.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (lightMaps.isChecked()){
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("LightMaps", true);
				}
				else{
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("LightMaps", false);
				}
			}
		});
		fps.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (fps.isChecked()){
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("FPS", true);
				}
				else{
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("FPS", false);
				}
			}
		});
		pureServers.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (pureServers.isChecked()){
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("PureServers", true);
				}
				else{
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("PureServers", false);
				}
			}
		});
		protocol.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (protocol.isChecked()){
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("Protocol", true);
				}
				else{
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("Protocol", false);
				}
			}
		});
	}
	
	private void checkPreferences(){
		PlayerPreferences playerPreferences = PlayerPreferences.getThePlayerPreferences();
		if (playerPreferences.preferenceOn("Sound")){
			sound.setChecked(true);
		}
		else{
			sound.setChecked(false);
		}
		if (playerPreferences.preferenceOn("LightMaps")){
			lightMaps.setChecked(true);
		}
		else{
			lightMaps.setChecked(false);
		}
		if (playerPreferences.preferenceOn("FPS")){
			fps.setChecked(true);
		}
		else{
			fps.setChecked(false);
		}
		if (playerPreferences.preferenceOn("PureServers")){
			pureServers.setChecked(true);
		}
		else{
			pureServers.setChecked(false);
		}
		if (playerPreferences.preferenceOn("Protocol")){
			protocol.setChecked(true);
		}
		else{
			protocol.setChecked(false);
		}
	}

}
