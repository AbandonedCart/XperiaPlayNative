package quake.two.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import quake.two.controls.*;

import quake.two.controls.Control;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class QuakeView extends GLSurfaceView {
	/**
	 * Key states (read by the native side). DO NOT rename without modifying the
	 * native side.
	 */
	private static final int MAX_KEY_EVENTS = 128;
	public int[] keyEvents = new int[MAX_KEY_EVENTS];
	public int eventId = 0;
	private boolean inGame = true;
	private float touchDownX = 0;
	private float touchDownY = 0;
	private float pitch = 0;
	private float yaw = 0;
	private float roll = 0;
	private boolean isXOkeysSwapped = false;
	/*
	 *  Key codes understood by the Quake engine.
	 */
	public static final int K_TAB = 9;
	public static final int K_ENTER = 13;
	public static final int K_ESCAPE = 27, K_SPACE = 32, K_BACKSPACE = 127;
	public static final int K_UPARROW = 128;
	public static final int K_DOWNARROW = 129;
	public static final int K_LEFTARROW = 130;
	public static final int K_RIGHTARROW = 131;
	public static final int K_ALT = 132, K_CTRL = 133, K_SHIFT = 134, K_F1 = 135, K_F2 = 136, K_F3 = 137,
			K_F4 = 138, K_F5 = 139, K_F6 = 140, K_F7 = 141, K_F8 = 142, K_F9 = 143, K_F10 = 144,
			K_F11 = 145, K_F12 = 146, K_INS = 147, K_DEL = 148, K_PGDN = 149, K_PGUP = 150, K_HOME = 151,
			K_END = 152, K_KP_HOME = 160, K_KP_UPARROW = 161, K_KP_PGUP = 162, K_KP_LEFTARROW = 163, K_KP_5 = 164, K_KP_RIGHTARROW = 165,
			K_KP_END = 166, K_KP_DOWNARROW = 167, K_KP_PGDN = 168, K_KP_ENTER = 169, K_KP_INS = 170, K_KP_DEL = 171, K_KP_SLASH = 172,
			K_KP_MINUS = 173, K_KP_PLUS = 174, K_MOUSE1 = 200, K_MOUSE2 = 201, K_MOUSE3 = 202, K_MOUSE4 = 241, K_MOUSE5 = 242,
			K_JOY1 = 203, K_JOY2 = 204, K_JOY3 = 205, K_JOY4 = 206, K_AUX1 = 207, K_AUX2 = 208, K_AUX3 = 209,
			K_AUX4 = 210, K_AUX5 = 211, K_AUX6 = 212, K_AUX7 = 213, K_AUX8 = 214, K_AUX9 = 215, K_AUX10 = 216,
			K_AUX11 = 217, K_AUX12 = 218, K_AUX13 = 219, K_AUX14 = 220, K_AUX15 = 221, K_AUX16 = 222, K_AUX17 = 223,
			K_AUX18 = 224, K_AUX19 = 225, K_AUX20 = 226, K_AUX21 = 227, K_AUX22 = 228, K_AUX23 = 229, K_AUX24 = 230,
			K_AUX25 = 231, K_AUX26 = 232, K_AUX27 = 233, K_AUX28 = 234, K_AUX29 = 235, K_AUX30 = 236, K_AUX31 = 237,
			K_AUX32 = 238, K_MWHEELDOWN = 239, K_MWHEELUP = 240, K_PAUSE = 255, K_LAST = 256;
	public static final int XPERIA_PLAY_L1 = 102;	// space
	public static final int XPERIA_PLAY_R1 = 103;	// ctrl
	public static final int XPERIA_PLAY_X = 23;	// enter
	public static final int XPERIA_PLAY_SQUARE = 99;	// [
	public static final int XPERIA_PLAY_O = 4;	// && (AKeyEvent_getMetaState(event) == AMETA_ALT_ON) ]
	public static final int XPERIA_PLAY_START = 108;	// ]
	public static final int XPERIA_PLAY_TRIANGLE = 100;	// c

	private Quake2 game;
	private TouchpadControl touchpadControl;

	public QuakeView(Quake2 quake2) {
		super(quake2);
		setGame(quake2);
		isXOkeysSwapped = isXOkeysSwapped();
		touchpadControl = new TouchpadControl(this);
	}

	public void postKeyEvent(int key, int down)
	{
		synchronized (keyEvents){
			if (eventId < keyEvents.length){
				keyEvents[eventId++] = key | down<<8;
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int code = convertCode(keyCode, event);
		if (getGame().isDebug()){
			Log.i("Quake2.java", "onKeyDown: " + keyCode + " code: " + code);
		}
		if (code < 0){
			Intent intent = new Intent(getContext(), Menu.class);
			getContext().startActivity(intent);
		}
		else if (code < K_LAST){
			postKeyEvent(code, 1);
			return true;
		}
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		int code = convertCode(keyCode, event);
		if (getGame().isDebug()){
			Log.i("Quake2.java", "onKeyUp: " + keyCode + " code: " + code);
		}
		if (code > 0 && code < K_LAST){
			postKeyEvent(code, 0);
			return true;
		}
		return false;
	}

	private int convertCode(int keyCode, KeyEvent event) {
		int code = 0;
		switch (keyCode){
		//case KeyEvent.KEYCODE_J:
		case KeyEvent.KEYCODE_DPAD_LEFT:
			code = K_LEFTARROW;
			break;
			//case KeyEvent.KEYCODE_K:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			code = K_RIGHTARROW;
			break;
			//case KeyEvent.KEYCODE_I:
		case KeyEvent.KEYCODE_DPAD_UP:
			code = K_UPARROW;
			break;
			//case KeyEvent.KEYCODE_M:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			code = K_DOWNARROW;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			code = K_ENTER;
			postKeyEvent('y', event.getAction());
			break;
		case KeyEvent.KEYCODE_ENTER:
			code = K_ENTER;
			postKeyEvent('y', event.getAction());
			break;
		case KeyEvent.KEYCODE_TAB:
			code = K_TAB;
			break;
		case KeyEvent.KEYCODE_BACK:
			code = K_ESCAPE;
			break;
		case KeyEvent.KEYCODE_DEL:
			//code = K_DEL;
			code = K_BACKSPACE;
			break;
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			code = K_SHIFT;
			break;
		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
			code = K_ALT;
			break;
		case KeyEvent.KEYCODE_STAR:
			code = '*';
			break;
		case KeyEvent.KEYCODE_PLUS:
			code = K_KP_PLUS;
			break;
		case KeyEvent.KEYCODE_MINUS:
			code = K_KP_MINUS;
			break;
		case KeyEvent.KEYCODE_SLASH:
			code = K_KP_SLASH;
			break;
		case XPERIA_PLAY_L1:
			code = ' ';
			break;
		case XPERIA_PLAY_R1:
			code = K_CTRL;
			break;
		case XPERIA_PLAY_SQUARE:
			code = '/';
			break;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU){
			code = -1;
		}
		if (code==0){
			// normal keys should be passed as lowercased ascii
			code = event.getUnicodeChar();
			if ( code<32 || code>127){
				code = 0;
			}
			if ( code >= 'A' && code <= 'Z'){
				code = code - 'A' + 'a';
			}
			/*
                if ((keyCode >= KeyEvent.KEYCODE_A) && (keyCode <= KeyEvent.KEYCODE_Z)) {
                    code = keyCode - KeyEvent.KEYCODE_A + 'a';
                } else if ((keyCode >= KeyEvent.KEYCODE_0) && (keyCode <= KeyEvent.KEYCODE_9)) {
                    code = keyCode - KeyEvent.KEYCODE_0 + '0';
                } 
			 */
		}
		return code;
	}
	//public boolean onTrackballEvent(final MotionEvent e)
	
	public void onTouchPadEvent(MotionEvent event) {
		for (int p = 0; p < event.getPointerCount(); p++){
			touchpadControl.touched(new MultiMotionEvent(event, p));
		}
		//onTouchEvent(event);
	}

	public boolean onTouchEvent(final MotionEvent event) {
		boolean result = false;
		if (inGame){
			onStartTouch();
			for (int p = 0; p < event.getPointerCount(); p++){
				result = inGameTouchEvent(new MultiMotionEvent(event, p));
			}
			onEndTouch();
		}
		else{
			//result = queueTouchEvent(event.getAction(), event.getX(), event.getY(), event.getPressure());
		}
		//Log.d("Quake_JAVA", "onTouchEvent action=" + event.getAction() + " x=" + event.getX() + " y=" + event.getY() + " pressure=" + event.getPressure() + "size = " + event.getSize());
		/* Perhaps we should pass integers down to reduce the number of float computations */
		return result;
	}
	
	public void onStartTouch(){
		for (Control control : game.getControls()){
			if (control.isOn()){
				control.onStartTouch();
			}
		}
	}
	
	public void onEndTouch(){
		for (Control control : game.getControls()){
			if (control.isOn()){
				control.onEndTouch();
			}
		}
	}
	
	public boolean inGameTouchEvent(MultiMotionEvent event){
		boolean result = true;
		List<Control> controls = touchedControls(event);
		for (Control control : controls){
			if (control.getQuakeView() != null){
				control.touchEvent(event);
			}
		}
		return result;
	}
	
	public List<Control> touchedControls(MultiMotionEvent event){
		List<Control> result;
		List<Control> controls = new Vector<Control>();
		List<Control> blockingControls = new Vector<Control>();
		for (Control control : game.getControls()){
			if (control.isOn()){
				if (!control.isBlocking() && control.touched(event)){
					controls.add(control);
				}
				else if (control.isBlocking() && control.touched(event)){
					blockingControls.add(control);
				}
			}
		}
		if (blockingControls.isEmpty()){
			result = controls;
		}
		else{
			result = blockingControls;
		}
		return result;
	}
	
	public void killBrokenTouchEvents(){
		for (Control control : game.getControls()){
			if (control.getQuakeView() != null && control.isOn()){
				control.killBrokenTouchEvent();
			}
		}
		touchpadControl.killBrokenTouchEvent();
	}

	public void kbdUpdate() {
		//Log.i("Quake2.java", "kbdUpdate");
		synchronized (keyEvents) {
			for (int k=0;k<eventId;k++){
				int event = keyEvents[k];
				int key = event & 0x00ff;
				int down = (event>>8) & 0x00ff;
				if (getGame().isDebug()){ 
					Log.i("Quake2.java", String.format("keyEvent %d %d" ,key, down ));
				}
				Log.i("Quake2.java", String.format("keyEvent %d %d" ,key, down ));
				Quake2.Quake2KeyEvent(key, down);
			}
			// Clear the event buffer size
			eventId = 0;
		}
	}

	public void setGame(Quake2 game) {
		this.game = game;
	}

	public Quake2 getGame() {
		return game;
	}
	
	private final char DEFAULT_O_BUTTON_LABEL = 0x25CB;   //hex for WHITE_CIRCLE
	
	private boolean isXOkeysSwapped() {
	    boolean flag = false;
	    return false;
	}

	public boolean queueMotionEvent(int action, float x, float y, float pressure, int width, int height){
		float tempX = x/width;
		float tempY = y/height;
		if (action == MotionEvent.ACTION_DOWN){
			touchDownX = tempX;
			touchDownY = tempY;
		}
		else if (action == MotionEvent.ACTION_MOVE){
			float scale = 180.0f;
			// compute yaw from touchscreen X;
			float tx = scale * (tempX - touchDownX);
			touchDownX = tempX;
			// compute pitch from touchscreen Y
			float ty = scale * (tempY - touchDownY);
			touchDownY = tempY;
			pitch += ty;
			setYaw(-tx);
			roll = 0;
			Quake2.Quake2MoveEvent(2, 0, 0, 0, pitch, yaw, roll);
		}
		return true;
	}
	
	public void queueTouchpadEvent(int action, float x, float y, int width, int height) {
		float tempX = x/width;
		float tempY = y/height;
		if (action == MotionEvent.ACTION_MOVE){
			float scale = 180.0f;
			// compute yaw from touchscreen X;
			float tx = scale * (tempX);
			// compute pitch from touchscreen Y
			float ty = scale * (tempY);
			pitch += -ty;
			setYaw(-tx);
			roll = 0;
			Quake2.Quake2MoveEvent(2, 0, 0, 0, pitch, yaw, roll);
		}
	}

	public boolean isInGame(){
		return inGame;
	}

	public void refreash() {
		touchpadControl.refreash();
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	public void setroll(float roll) {
		this.roll = roll;
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public float getRoll() {
		return roll;
	}
}
