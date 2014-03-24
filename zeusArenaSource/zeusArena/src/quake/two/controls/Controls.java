package quake.two.controls;

import java.util.List;
import java.util.Vector;

import org.zeus.arena.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import quake.two.android.*;
public class Controls extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controls);
		
		checkPreferences();
		Button defaults = (Button)findViewById(R.id.defaults);
		defaults.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				restoreDefaults();
			}
		});
		Button arrange = (Button)findViewById(R.id.arrangeButton);
		arrange.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),ControlLayout.class);
				startActivity(intent);
			}
		});
		RadioGroup improvedControls = (RadioGroup) findViewById(R.id.radioGroup1);
		improvedControls.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				List<String> values = new Vector<String>(); 
				if (checkedId == R.id.improvedControls){
					values.add("Improved");
				}
				else if(checkedId == R.id.traditionalControls){
					values.add("Traditional");
				}
				else{
					values.add("None");
				}
				PlayerPreferences.getThePlayerPreferences().updatePreference("Scheme", values);
			}
		});
		
		RadioGroup swipeGroup = (RadioGroup) findViewById(R.id.radioGroup2);
		swipeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio0){
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("Swipe", true);
				}
				else{
					PlayerPreferences.getThePlayerPreferences().setPreferenceOn("Swipe", false);
				}
			}
		});
		
		final CheckBox vibrations = (CheckBox) findViewById(R.id.checkBox1);
		vibrations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (vibrations.isChecked()){
					List<String> values = new Vector<String>();
					values.add("On");
					PlayerPreferences.getThePlayerPreferences().updatePreference("Vibrations", values);
				}
				else{
					List<String> values = new Vector<String>();
					values.add("Off");
					PlayerPreferences.getThePlayerPreferences().updatePreference("Vibrations", values);
				}
			}
		});
	}
	
	private void checkPreferences(){
		if (Persistence.getPersistence().getGame().getControlScheme() == ControlScheme.IMPROVED){
			RadioButton improvedButton = (RadioButton) findViewById(R.id.improvedControls);
			improvedButton.setChecked(true);
		}
		else if(Persistence.getPersistence().getGame().getControlScheme() == ControlScheme.TRADITIONAL){
			RadioButton traditionalButton = (RadioButton) findViewById(R.id.traditionalControls);
			traditionalButton.setChecked(true);
		}
		else{
			RadioButton noneButton = (RadioButton) findViewById(R.id.noControls);
			noneButton.setChecked(true);
		}
		if(Persistence.getPersistence().getGame().vibrationOn()){	//if vibrations are on
			CheckBox vibrations = (CheckBox) findViewById(R.id.checkBox1);
			vibrations.setChecked(true);
		}
		else{
			CheckBox vibrations = (CheckBox) findViewById(R.id.checkBox1);
			vibrations.setChecked(false);
		}
		
		if (PlayerPreferences.getThePlayerPreferences().preferenceOn("Swipe")){
			RadioButton swipeButton = (RadioButton) findViewById(R.id.radio0);
			swipeButton.setChecked(true);
		}
		else{
			RadioButton analogButton = (RadioButton) findViewById(R.id.radio1);
			analogButton.setChecked(true);
		}
	}
	
	private void restoreDefaults()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to reset to the default settings? \n (this will reset startup settings as well)");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				PlayerPreferences.getThePlayerPreferences().restoreDefaults();
				checkPreferences();
			}
		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}