/*
 * Kwaak3
 * Copyright (C) 2010 Roderick Colenbrander
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.zeus.arena;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.zeus.arena.R;
import org.zeus.arena.controls.ChangeWeaponControl;
import org.zeus.arena.controls.ControlScheme;
import org.zeus.arena.controls.FireControl;
import org.zeus.arena.controls.ImprovedFireControl;
import org.zeus.arena.controls.ImprovedJumpControl;
import org.zeus.arena.controls.ImprovedLookControl;
import org.zeus.arena.controls.JumpControl;
import org.zeus.arena.controls.LeftAnalogControl;
import org.zeus.arena.controls.LookControl;
import org.zeus.arena.controls.StationaryFire;
import org.zeus.arena.controls.UseItemControl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NativeActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent.PointerCoords;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Game extends NativeActivity{
	private KwaakAudio mKwaakAudio;
	private KwaakView mGLSurfaceView;
	private RelativeLayout controlsLayout;
	private List<Control> controls;
	private boolean controlLockOn = false;
	private int screenWidth;
	private int screenHeight;
	private Vibrator vibrator;
	private String sdcardPath;
	private NativeMotionEvent nativeMotionEvent;
	public ProgressDialog dialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		sdcardPath = Environment.getExternalStorageDirectory().getPath();
		Persistence.setGame(this);
		if (checkGameFiles()){
        	dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			screenHeight = displaymetrics.heightPixels;
			screenWidth = displaymetrics.widthPixels;
			PlayerPreferences.makeThePlayerPreferences(screenWidth, screenHeight);
			/* We like to be fullscreen */
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    	
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			getWindow().takeSurface(null);
			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			RegisterThis();
	
			setupControls();
			mGLSurfaceView = new KwaakView(this, this);
			setContentView(mGLSurfaceView);
			mGLSurfaceView.requestFocus();
			mGLSurfaceView.setId(1);
			
			for (Control control : controls){
				control.setView(mGLSurfaceView);
			}
			
			controlsLayout = new RelativeLayout(this);
			addContentView(controlsLayout, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	
			/* The KwaakAudio object is owned by the Game class but is only used
			 * by the game library using JNI calls. It is not that pretty but it
			 * is the only way without using the (unstable) AudioTrack API from C++.
			 */
			mKwaakAudio = new KwaakAudio();
			KwaakJNI.setAudio(mKwaakAudio);
			getStartupOptions();
		}
		else{
        	super.onCreate(savedInstanceState);
        	Intent intent = new Intent(this,GameNotFound.class);
			startActivity(intent);
			finish();
        }
	}
	
	public List<Control> getControls(){
		return controls;
	}
	
	public ControlScheme getControlScheme(){
		ControlScheme result = ControlScheme.NONE;
		List<String> values = PlayerPreferences.getThePlayerPreferences().getPreference("Scheme");
		if (values.get(0).equals("Improved")){
			result = ControlScheme.IMPROVED;
		}
		else if(values.get(0).equals("Traditional")){
			result = ControlScheme.TRADITIONAL;
		}
		return result;
	}
	
	public boolean vibrationOn(){
		boolean result = true;
		List<String> preferences = PlayerPreferences.getThePlayerPreferences().getPreference("Vibrations");
		if(preferences.get(0).equals("On")){	//if vibrations are on
			result = true;
		}
		else{
			result = false;
		}
		return result;
	}
	
	public void setupControls(){
		controlLockOn = true;
		controls = new Vector<Control>();
		ControlScheme scheme = getControlScheme();
		if (scheme != ControlScheme.NONE){
			if (scheme == ControlScheme.IMPROVED){
				if (vibrationOn()){
					controls.add(new ImprovedFireControl(vibrator));
				}
				else{
					controls.add(new ImprovedFireControl(null));
				}
				controls.add(new ImprovedJumpControl());
				controls.add(new StationaryFire());
			}
			else if (scheme == ControlScheme.TRADITIONAL){
				controls.add(new LeftAnalogControl());
				controls.add(new FireControl());
				controls.add(new JumpControl());
			}
			controls.add(new ChangeWeaponControl());
			controls.add(new UseItemControl());
			controls.add(new ImprovedLookControl(screenWidth));
		}
		else{
			controls.add(new LookControl());
		}
		controlLockOn = false;
	}
	
	public boolean isControlLockOn(){
		return controlLockOn;
	}
	
	public void gameStateChanged(boolean inGame){
		final boolean tempShow = inGame;
		final Context context = this;
		controlsLayout.post(new Runnable(){ 
			public void run(){
				controlsLayout.removeAllViews();
				if (tempShow){
					for (Control control : controls){
						RelativeLayout.LayoutParams params = control.getLayoutParams(screenWidth, screenHeight);
						if (params != null){
							controlsLayout.addView(control.getImageView(context), params);
						}
					}
				}
			}
		});
	}
	
	public void refreashControl(Control control){
		final Context context = this;
		final Control tempControl = control;
		controlsLayout.post(new Runnable(){ 
			public void run(){
				View controlView = tempControl.getImageView(context);
				//controlsLayout.removeView(controlView);
				RelativeLayout.LayoutParams params = tempControl.getLayoutParams(screenWidth, screenHeight);
				if (params != null){
					//controlsLayout.addView(controlView, params);
					controlView.setLayoutParams(params);
					controlView.invalidate();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		//Log.d("Quake_JAVA", "onPause");
		super.onPause();

		if(mKwaakAudio != null)
			mKwaakAudio.pause();
	}

	@Override
	protected void onResume() {
		/* Resume doesn't always seem to work well. On my Milestone it works
		 * but not on the G1. The native code seems to be running but perhaps
		 * we need to issue a 'vid_restart'.
		 */
		//Log.d("Quake_JAVA", "onResume");
		super.onResume();
		if(mGLSurfaceView != null)
		{
			mGLSurfaceView.onResume();
		}

		if(mKwaakAudio != null)
			mKwaakAudio.resume();
	}
	
	public void refreash(){
		if (mGLSurfaceView != null){
			gameStateChanged(mGLSurfaceView.isInGame());
			setupControls();
			for (Control control : controls){
				control.setView(mGLSurfaceView);
			}
			mGLSurfaceView.refreash();
		}
	}
	
	public void getStartupOptions(){
		PlayerPreferences playerPreferences = PlayerPreferences.getThePlayerPreferences();
		if (playerPreferences.preferenceOn("Sound")){
			KwaakJNI.enableAudio(true);
		}
		else{
			KwaakJNI.enableAudio(false);
		}
		if (playerPreferences.preferenceOn("LightMaps")){
			KwaakJNI.enableLightmaps(true);
		}
		else{
			KwaakJNI.enableLightmaps(false);
		}
		if (playerPreferences.preferenceOn("FPS")){
			KwaakJNI.showFramerate(true);
		}
		else{
			KwaakJNI.showFramerate(false);
		}
		if (playerPreferences.preferenceOn("PureServers")){
			KwaakJNI.enablePureServers(true);
		}
		else{
			KwaakJNI.enablePureServers(false);
		}
		if (playerPreferences.preferenceOn("Protocol")){
			KwaakJNI.enableOpenArenaProtocol(true);
		}
		else{
			KwaakJNI.enableOpenArenaProtocol(false);
		}
	}
	
	public void OnNativeKeyPress(int action, int keyCode, int metaState){
		//KeyEvent keyEvent = new KeyEvent(action, keyCode);
		long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        KeyEvent keyEvent = new KeyEvent(
            downTime, 
            eventTime, 
            action,
            keyCode, 
            0, 
            metaState
        );
		mGLSurfaceView.dispatchKeyEvent(keyEvent);
	}

	public void OnNativeMotion(int action, int x, int y, int source, int device_id, boolean newEvent) {
		if (newEvent){
			if (nativeMotionEvent != null && mGLSurfaceView != null){
				nativeMotionEvent.dispatchMotionEvent(mGLSurfaceView);
			}
			boolean touchPadEvent = false;
			if(source == 1048584){	//touchpad
				touchPadEvent = true;
			}
			nativeMotionEvent = new NativeMotionEvent(touchPadEvent);
		}
		nativeMotionEvent.addPointer(action, x, y, source, device_id);
	 }
	
	private boolean checkGameFiles()
	{
		boolean result = true;
		File baseq3_dir = new File(sdcardPath + "/quake3/baseq3");
		File temp_dir = new File(sdcardPath + "/quake3/temp/");
		if(!baseq3_dir.exists()){
			result = false;
		}
		if (temp_dir.exists()){
			result = false;
		}
		return result;
	}
	
	public native int RegisterThis();
	
	static {
		System.loadLibrary("kwaakjni");
	}

	public void showKeyboard() {
		mGLSurfaceView.showKeyboard();
	}

	public void displayKeyboard() {
		InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(mGLSurfaceView, InputMethodManager.SHOW_FORCED);
	}
}
