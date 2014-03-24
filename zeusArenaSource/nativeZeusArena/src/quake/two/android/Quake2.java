/*
 * Copyright (C) 2009 jeyries@yahoo.fr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quake.two.android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import org.zeus.arena.GameNotFound;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NativeActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock; 
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import quake.two.controls.*;
 
public class Quake2 extends NativeActivity{
	
	private QuakeView mGLSurfaceView = null;
	private QuakeRenderer mRenderer  = null;
	private Vibrator vibrator;
	private boolean please_exit = false;
	// android settings - saved as preferences
	private boolean debug = false;
	private boolean enable_audio = true;
	private boolean enable_sensor = true;
	private boolean enable_vibrator = false;
	private boolean enable_ecomode = false;
	private long tstart;
	private int timelimit = 0; //4*60000;
	private String error_message;
	private int overlay = 0;
	private Handler handlerUI;	/// Handler for asynchronous message
	public static final int MSG_SHOW_DIALOG = 1;	/// => showDialog
	static final int DIALOG_EXIT_ID = 0;
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_PAK_NOT_FOUND = 2;
	static final int DIALOG_ERROR = 3;
	static final int DIALOG_LOADING = 4;
	static final int DIALOG_CHECK_UPDATE = 5;
	private Object sensorEvents = new Object();
	private float pitch = PITCH_DEFAULT, roll = 0.0f;
	private float pitch_ref = PITCH_DEFAULT;
	private int touch_state = 0;
	private float touch_x, touch_y;
	private float touch_xref, touch_yref ;
	public static final int MOVE_NOTHING = 0;
	public static final int MOVE_FORWARDMOVE = 1;
	public static final int MOVE_YAW = 2;
	public static final int MOVE_VIEW = 3;
	private int move_state = MOVE_NOTHING;
	public static final float PITCH_DEFAULT = -50.0f;
	private float qpitch = 0.0f; // set	
	private List<Control> controls;
	private RelativeLayout controlsLayout;
	private int screenHeight;
	private int screenWidth;
	public ProgressDialog dialog;
	private NativeMotionEvent nativeMotionEvent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState){
		Persistence.setGame(this);
    	dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;
		PlayerPreferences.makeThePlayerPreferences(screenWidth, screenHeight);
		// fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// keep screen on 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState); 
		getWindow().takeSurface(null);
		Log.d("TESTING", "NOW");
		RegisterThis();
		start_quake2();
	}

	public void copy_asset(boolean overwrite, String name)
	{  	
		if (overwrite ||!(new File("/sdcard/baseq2/"+name)).exists()){
			copy_asset( name,"/sdcard/baseq2/"+name);
		}
	}

	public void copy_asset(String name_in, String name_out)
	{
		Log.i("Quake2.java", String.format("copy_asset %s to %s", name_in, name_out));
		AssetManager assets = this.getAssets();
		try {
			InputStream in = assets.open(name_in);
			OutputStream out = new FileOutputStream(name_out);
			copy_stream(in, out);		
			out.close();
			in.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copy_stream( InputStream in, OutputStream out )
	throws IOException
	{
		byte[] buf = new byte[1024];    	
		while(true){
			int count = in.read(buf);
			if (count<=0) break;
			out.write(buf, 0, count);
		}
	}

	public void start_quake2() {
		// check PAK file
		if (!(new File("/sdcard/baseq2/pak0.pak")).exists() && !(new File("/sdcard/baseq2/pics/colormap.pcx")).exists()){
			setContentView(org.zeus.arena.R.layout.game2notfound);
		}
		else{
			// check CFG file if not present, copy it silently
			copy_asset(false, "config.cfg");
			copy_asset(true, "overlay1.tga");
			copy_asset(true, "overlay2.tga");
			copy_asset(true, "overlay3.tga");
			//showDialog(DIALOG_LOADING);
			setVibrator((Vibrator)getSystemService(Context.VIBRATOR_SERVICE));
			// Create our Preview view and set it as the content of our Activity
			setupControls();
			setmGLSurfaceView(new QuakeView(this));
			//mGLSurfaceView.setGLWrapper( new MyWrapper());
			//mGLSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
			//setEGLConfigChooser  (int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize)
			//mGLSurfaceView.setEGLConfigChooser(8,8,8,0,16,0);
			getmGLSurfaceView().setEGLConfigChooser(new QuakeEGLConfigChooser(this));
			mRenderer = new QuakeRenderer(this, mGLSurfaceView);
			mRenderer.setSpeed_limit(enable_ecomode ? 40 : 0);
			getmGLSurfaceView().setRenderer(mRenderer);
			// This will keep the screen on, while your view is visible. 
			getmGLSurfaceView().setKeepScreenOn(true);
			setContentView(getmGLSurfaceView());
			getmGLSurfaceView().requestFocus();
			getmGLSurfaceView().setFocusableInTouchMode(true);
			for (Control control : controls){
				control.setView(mGLSurfaceView);
			}
			controlsLayout = new RelativeLayout(this);
			gameStateChanged(true);
			addContentView(controlsLayout, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		}
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
	
	@Override
	protected void onPause() {
		Log.i( "Quake2.java", "onPause" );
		super.onPause();
		/*please_exit = true;
		if (mRenderer.getState()!=mRenderer.STATE_RESET){        	
			getmGLSurfaceView().queueEvent(new Runnable(){
				public void run() {
					getmGLSurfaceView().onPause();
					// I/Quake2.java(13899): Quake2Quit
					// D/libquake2.so(13899): R_Shutdown
					// E/libEGL  (13899): call to OpenGL ES API with no current context (logged once per thread)
					sQuake2Quit();
					System.exit(0); // kill process will force reload library on next launch
				}});
		}*/
	}

	@Override
	protected void onResume() {
		Log.i( "Quake2.java", "onResume" );
		super.onResume();
		if (mRenderer != null){
			if (mRenderer.getState()!=mRenderer.STATE_RESET){
				getmGLSurfaceView().onResume();
			}
		}
	}

	@Override
	protected void onRestart() {
		Log.i( "Quake2.java", "onRestart" );
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Log.i( "Quake2.java", "onStop" );
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.i( "Quake2.java", "onDestroy" );
		super.onDestroy();
	}

	///////////////// JNI methods /////////////////////////////
	/* this is used to load the 'quake2' library on application
	 * startup. The library has already been unpacked into
	 * /data/data/com.example.HelloJni/lib/liquake2.so at
	 * installation time by the package manager.
	 */
	static {
		//System.loadLibrary("quake2");
		try {
			Log.i("JNI", "Trying to load libquake2.so");
			System.loadLibrary("quake2");
		}
		catch (UnsatisfiedLinkError ule) {
			Log.e("JNI", "WARNING: Could not load libquake2.so");
		}
	}
	/* A native method that is implemented by the
	 * 'quake2' native library, which is packaged
	 * with this application.
	 */
	// synchronized access

	private static Object quake2Lock = new Object();

	private static int sQuake2Init(){
		int ret;
		synchronized(quake2Lock) { 	
			ret = Quake2Init();
		}
		return ret;
	}

	public static int sQuake2Frame(){
		int ret;
		synchronized(quake2Lock) { 	
			ret = Quake2Frame();
		}
		return ret;
	}

	private static int sQuake2Quit(){
		int ret;
		synchronized(quake2Lock) { 	
			Log.i( "Quake2.java", "Quake2Quit" );
			ret = Quake2Quit();
		}
		return ret;
	}

	private static int sQuake2PaintAudio( ByteBuffer buf ){
		int ret;
		synchronized(quake2Lock) { 	
			ret = Quake2PaintAudio(buf);
		}
		return ret;
	}
	// raw acces

	public static native String Quake2GetVersion();

	public static native int Quake2Init();

	public static native int Quake2Frame();

	public static native int Quake2Quit();

	public static native int Quake2Test();

	public static native void Quake2SetWidth(int value);

	public static native void Quake2SetHeight(int value);

	public static native void Quake2SetOverlay(int value);

	public static native int Quake2PaintAudio(ByteBuffer buf);

	public static native int Quake2GetDisableScreen();

	public static native int Quake2GetVibration();

	
	public native int RegisterThis();

	public static native void Quake2KeyEvent(int key, int down);

	public static native void Quake2MoveEvent(int mode, 
			int forwardmove, int sidemove, int upmove,
			float pitch, float yaw, float roll);

	public static native int Quake2Paused();

	/*----------------------------
	 * Audio
	 *----------------------------*/

	public void audio_thread() throws IOException{
		int audioSize = (2048*4); 
		ByteBuffer audioBuffer = ByteBuffer.allocateDirect(audioSize);
		byte[] audioData = new byte[audioSize];
		// output to a PCM file
		// adb pull /sdcard/quake2.pcm .
		// sox -L -s -2 -c 2 -r 44100 -t raw quake2.pcm -t wav quake2.wav
		//FileOutputStream out = new FileOutputStream( new File( "/sdcard/quake2.pcm") );
		AudioTrack oTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 22050, //44100,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, 
				4*(22050/5), // 200 millisecond buffer
				// => impact on audio latency
				AudioTrack.MODE_STREAM);
		Log.i("Quake2", "start audio");
		// Start playing data that is written
		oTrack.play();
		long tstart = SystemClock.uptimeMillis();
		while (!please_exit){
			long tnow = SystemClock.uptimeMillis() ;
			// timelimit
			if (getTimelimit()!=0 && (tnow-tstart) > getTimelimit()){
				break;
			}
			sQuake2PaintAudio( audioBuffer );          	    
			audioBuffer.position(0);
			audioBuffer.get(audioData);
			// Write the byte array to the track
			oTrack.write(audioData, 0, audioData.length);    
		}
		Log.i("Quake2", "stop audio");
		// Done writting to the track
		oTrack.stop();	    
	}

	/* Quake 2 angle definition :
	// angle indexes
	#define	PITCH				0		// up / down
	#define	YAW					1		// left / right
	#define	ROLL				2		// fall over */

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	public String getError_message() {
		return error_message;
	}

	public void setHandlerUI(Handler handlerUI) {
		this.handlerUI = handlerUI;
	}

	public Handler getHandlerUI() {
		return handlerUI;
	}

	public void setTstart(long tstart) {
		this.tstart = tstart;
	}

	public long getTstart() {
		return tstart;
	}

	public void setTimelimit(int timelimit) {
		this.timelimit = timelimit;
	}

	public int getTimelimit() {
		return timelimit;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setPd_loading(ProgressDialog dialog) {
		this.dialog = dialog;
	}

	public ProgressDialog getPd_loading() {
		return dialog;
	}

	public void setEnable_audio(boolean enable_audio) {
		this.enable_audio = enable_audio;
	}

	public boolean isEnable_audio() {
		return enable_audio;
	}

	public void setOverlay(int overlay) {
		this.overlay = overlay;
	}

	public int getOverlay() {
		return overlay;
	}

	public void setmGLSurfaceView(QuakeView mGLSurfaceView) {
		this.mGLSurfaceView = mGLSurfaceView;
	}

	public QuakeView getmGLSurfaceView() {
		return mGLSurfaceView;
	}

	public void setEnable_vibrator(boolean enable_vibrator) {
		this.enable_vibrator = enable_vibrator;
	}

	public boolean isEnable_vibrator() {
		return enable_vibrator;
	}

	public void setVibrator(Vibrator vibrator) {
		this.vibrator = vibrator;
	}

	public Vibrator getVibrator() {
		return vibrator;
	}

	public void setSensorEvents(Object sensorEvents) {
		this.sensorEvents = sensorEvents;
	}

	public Object getSensorEvents() {
		return sensorEvents;
	}

	public void setPitch_ref(float pitch_ref) {
		this.pitch_ref = pitch_ref;
	}

	public float getPitch_ref() {
		return pitch_ref;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getPitch() {
		return pitch;
	}

	public void setTouch_state(int touch_state) {
		this.touch_state = touch_state;
	}

	public int getTouch_state() {
		return touch_state;
	}

	public void setTouch_xref(float touch_xref) {
		this.touch_xref = touch_xref;
	}

	public float getTouch_xref() {
		return touch_xref;
	}

	public void setTouch_yref(float touch_yref) {
		this.touch_yref = touch_yref;
	}

	public float getTouch_yref() {
		return touch_yref;
	}

	public void setTouch_x(float touch_x) {
		this.touch_x = touch_x;
	}

	public float getTouch_x() {
		return touch_x;
	}

	public void setTouch_y(float touch_y) {
		this.touch_y = touch_y;
	}

	public float getTouch_y() {
		return touch_y;
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

	public List<Control> getControls() {
		return controls;
	}
	
	public void setupControls(){
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
			controls.add(new CrouchControl());
			controls.add(new YControl());
		}
		else{
			controls.add(new LookControl());
			controls.add(new YControl());
		}
	}

	public void showKeyboard() {
		mRenderer.showKeyboard();
	}

	public void displayKeyboard() {
		InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(mGLSurfaceView, InputMethodManager.SHOW_FORCED);
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

	public void setRoll(float roll) {
		this.roll = roll;
	}

	public float getRoll() {
		return roll;
	}
}


