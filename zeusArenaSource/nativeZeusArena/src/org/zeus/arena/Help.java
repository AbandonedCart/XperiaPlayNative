package org.zeus.arena;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Help extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		Button controls = (Button) findViewById(R.id.button1);
		controls.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),HelpDetails.class);
				intent.putExtra("details", getControlsDetails());
				startActivity(intent);
			}
		});
		Button mismatch = (Button) findViewById(R.id.button2);
		mismatch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),HelpDetails.class);
				intent.putExtra("details", getMismatchDetails());
				startActivity(intent);
			}
		});
		Button install = (Button) findViewById(R.id.button3);
		install.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),HelpDetails.class);
				intent.putExtra("details", getInstallDetails());
				startActivity(intent);
			}
		});
		Button online = (Button) findViewById(R.id.button4);
		online.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),HelpDetails.class);
				intent.putExtra("details", getOnlineDetails());
				startActivity(intent);
			}
		});
	}
	
	public String getControlsDetails(){
		String controls = "Two types of controls are supported: Traditional and Improved. "+'\n'
							+"Traditional works like most fps touch screen controls. "+'\n'
							+"However Improved controls are different in one key way: " +'\n'
							+"Movement and firing are handled by the same control. " +'\n'
							+"When using improved controls the movement "+'\n'
							+"control has a ring around it's edge that both moves and shoots. " +'\n'
							+"The phone will vibrate when you are touching close to the edge of the ring "+'\n'
							+"making it easier to tell when you are shifting between just moving and moving and shooting. " +'\n'
							+"If you need to shoot while standing still there "+'\n'
							+"is a smaller fire control below the move/fire control";
		return controls;
	}
	
	public String getMismatchDetails(){
		String mismatch = "If you are getting the: CLIENT/SERVER GAME MISMATCH error this" +'\n'+
							" is because you are trying to joining a pure server "+'\n' +
							"with different game files." +'\n'+
							" The ioquake3 engine checks to make sure you have the same game files" +'\n'+
							"installed before letting you join" + " pure servers. " +'\n'+
							"Try changing the pure server setting under start up options in this menu.";
		return mismatch;
	}
	
	public String getInstallDetails(){
		String install = "Create the directory baseq2 (Quake2Android) or quake3/baseq3 (ioquake3) in the root of your sdcard."+
							"Copy all .pk3 files from the game you want to install to the directory." +
							"The game is now installed, run the engine, if your game doesn't load check the mods section in the in game menu (ioquake3 only)";
		return install;
	}
	
	public String getOnlineDetails(){
		String online = "Most online servers for Open Arena are run using Open Arena's own engine, Zeus Arena uses"+
							"the ioquake3 engine. Open Arena and ioquake3 use seperate networking protocol numbers." +
							"Open Arena uses 71 and ioquake3 uses 68. Your networking protocol will default to 68 but" +
							"can be changed to 71. You can change your networking protocol in the startup options section" +
							"of this menu. However, if you change your networking protocol to Open Arenas (71) you will not" +
							"be able to play single player games until you change it back.";
		return online;
	}

}
