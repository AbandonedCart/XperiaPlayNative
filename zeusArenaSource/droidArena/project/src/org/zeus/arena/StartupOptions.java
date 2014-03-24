package org.zeus.arena;

import java.util.Arrays;
import java.util.List;
import org.zeus.arena.R;

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
					PlayerPreferences.getThePlayerPreferences().updatePreference("Sound", Arrays.asList("On"));
				}
				else{
					PlayerPreferences.getThePlayerPreferences().updatePreference("Sound", Arrays.asList("Off"));
				}
				game.getStartupOptions();
				game.refreash();
			}
		});
		lightMaps.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (lightMaps.isChecked()){
					PlayerPreferences.getThePlayerPreferences().updatePreference("LightMaps", Arrays.asList("On"));
				}
				else{
					PlayerPreferences.getThePlayerPreferences().updatePreference("LightMaps", Arrays.asList("Off"));
				}
				game.getStartupOptions();
				game.refreash();
			}
		});
		fps.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (fps.isChecked()){
					PlayerPreferences.getThePlayerPreferences().updatePreference("FPS", Arrays.asList("On"));
				}
				else{
					PlayerPreferences.getThePlayerPreferences().updatePreference("FPS", Arrays.asList("Off"));
				}
				game.getStartupOptions();
				game.refreash();
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
				game.refreash();
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
