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

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.zeus.arena.R;
import org.zeus.arena.controls.TouchpadControl;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class KwaakView extends GLSurfaceView {
	private KwaakRenderer mKwaakRenderer;
	private boolean inGame = false;
	private TouchpadControl touchpadControl;
	private Game game;
	private float ratioY;
	private float offset;
	private boolean isXOkeysSwapped = false;
	private long lastBack = 0;
	private long backInterval = 100;
	private long lastSearch = 0;
	
	public KwaakView(Context context, Game game){
		super(context);
		this.game = game;

		/* We need the path to the library directory for dlopen in our JNI library */
		String cache_dir, lib_dir;
		try {
			cache_dir = context.getCacheDir().getCanonicalPath();
			lib_dir = cache_dir.replace("cache", "lib");
		} catch (IOException e) {
			e.printStackTrace();
			lib_dir = "/data/data/org.droid.arena/lib";
		}
		KwaakJNI.setLibraryDirectory(lib_dir);
		
		mKwaakRenderer = new KwaakRenderer(this);
		setRenderer(mKwaakRenderer);
		KwaakJNI.setRenderer(mKwaakRenderer);

		setFocusable(true);
		setFocusableInTouchMode(true);
		isXOkeysSwapped = isXOkeysSwapped();
		touchpadControl = new TouchpadControl(this);
		
		refreashTurning();
	}
	
	public void refreashTurning(){
		if (PlayerPreferences.getThePlayerPreferences().preferenceOn("Turning")){
			QK_LEFT = QK_LEFT_MENU;
			QK_RIGHT = QK_RIGHT_MENU;
		}
		else{
			QK_LEFT = 'a';
			QK_RIGHT = 'd';
		}
	}
	
	public boolean isInGame(){
		return inGame;
	}
	
	public void refreash(){
		touchpadControl.refreash();
		refreashTurning();
	}
	
	public boolean onKeyPressed(int keyCode, KeyEvent event){
		boolean result = false;
		int qKeyCode = androidKeyCodeToQuake(keyCode, event);
		if (qKeyCode == -1){
			//result = queueKeyEvent(QK_ESCAPE, 1);
			Intent intent = new Intent(getContext(), Menu.class);
			getContext().startActivity(intent);
		}
		else{
			result = queueKeyEvent(qKeyCode, 1);
		}
		
		//Log.d("Quake_JAVA", "onKeyDown=" + keyCode + " " + qKeyCode + " " + event.getDisplayLabel() + " " + event.getUnicodeChar() + " " + event.getNumber());
		return result;
	}
	
	public boolean onKeyReleased(int keyCode, KeyEvent event) {
		int qKeyCode = androidKeyCodeToQuake(keyCode, event);
		//Log.d("Quake_JAVA", "onKeyUp=" + keyCode + " " + qKeyCode + " shift=" + event.isShiftPressed() + " =" + event.getMetaState());
		return queueKeyEvent(qKeyCode, 0);
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = false;
		int qKeyCode = androidKeyCodeToQuake(keyCode, event);
		if (qKeyCode == -1){
			//result = queueKeyEvent(QK_ESCAPE, 1);
			Intent intent = new Intent(getContext(), Menu.class);
			getContext().startActivity(intent);
		}
		else{
			result = queueKeyEvent(qKeyCode, 1);
		}
		//Log.d("Quake_JAVA", "onKeyDown=" + keyCode + " " + qKeyCode + " " + event.getDisplayLabel() + " " + event.getUnicodeChar() + " " + event.getNumber());
		return result;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		int qKeyCode = androidKeyCodeToQuake(keyCode, event);
		//Log.d("Quake_JAVA", "onKeyUp=" + keyCode + " " + qKeyCode + " shift=" + event.isShiftPressed() + " =" + event.getMetaState());
		return queueKeyEvent(qKeyCode, 0);
	}
	
	public void onTouchPadEvent(MotionEvent event) {
		//onStartTouch();
		for (int p = 0; p < event.getPointerCount(); p++){
			touchpadControl.touched(new MultiMotionEvent(event, p));
		}
		//onEndTouch();
	}

	//@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = false;
		if (inGame){
			onStartTouch();
			for (int p = 0; p < event.getPointerCount(); p++){
				result = inGameTouchEvent(new MultiMotionEvent(event, p));
			}
			onEndTouch();
		}
		else{
			float tempX = scaleX(event.getX());
			float tempY = scaleY(event.getY());
			result = queueTouchEvent(event.getAction(), tempX, tempY, event.getPressure());
		}
		//Log.d("Quake_JAVA", "onTouchEvent action=" + event.getAction() + " x=" + event.getX() + " y=" + event.getY() + " pressure=" + event.getPressure() + "size = " + event.getSize());
		/* Perhaps we should pass integers down to reduce the number of float computations */
		return result;
	}
	
	private float scaleX(float x){
		float menuWidth = 640;
		float menuHeight = 480;
		ratioY = menuHeight/((float)getHeight());
		offset = (((float)getWidth()) - (menuWidth/ratioY))/2;
		return (x - offset) * ratioY;
	}
	
	private float scaleY(float y){
		float menuHeight = 480;
		ratioY = menuHeight/((float)getHeight());
		return y*ratioY;
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
		if (!game.isControlLockOn()){
			for (Control control : game.getControls()){
				if (control.getKwaakView() != null && control.isOn()){
					control.killBrokenTouchEvent();
				}
			}
		}
		touchpadControl.killBrokenTouchEvent();
	}
	
	public boolean inGameTouchEvent(MultiMotionEvent event){
		boolean result = true;
		List<Control> controls = touchedControls(event);
		for (Control control : controls){
			if (control.getKwaakView() != null){
				control.touchEvent(event);
			}
		}
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
	
	public void setMenuState(int state){
		boolean oldState = inGame;
		if (state == 0){
			inGame = false;
		}
		else if (state == 1){
			inGame = true;
		}
		if (oldState != inGame){
			game.gameStateChanged(inGame);
		}
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		//Log.d("Quake_JAVA", "onTrackballEvent action=" + event.getAction() + " x=" + event.getX() + " y=" + event.getY());
		return queueTrackballEvent(event.getAction(), event.getX(), event.getY());
	}

	public boolean queueKeyEvent(final int qKeyCode, final int state)
	{
		if(qKeyCode == 0) return true;

		/* Make sure all communication with Quake is done from the Renderer thread */
        queueEvent(new Runnable(){
            public void run() {
        		KwaakJNI.queueKeyEvent(qKeyCode, state);
            }});
        return true;
	}

	public boolean queueMotionEvent(final int action, final float x, final float y, final float pressure)
	{
		/* Make sure all communication with Quake is done from the Renderer thread */
        queueEvent(new Runnable(){
            public void run() {
        		KwaakJNI.queueMotionEvent(action, x, y, pressure);
            }});
        return true;
	}
	
	public boolean queueTouchEvent(final int action, final float x, final float y, final float pressure)
	{
		/* Make sure all communication with Quake is done from the Renderer thread */
        queueEvent(new Runnable(){
            public void run() {
        		KwaakJNI.queueTouchEvent(action, x, y, pressure);
            }});
        return true;
	}
	
	public boolean queueTouchpadEvent(final int action, final float x, final float y)
	{
		/* Make sure all communication with Quake is done from the Renderer thread */
        queueEvent(new Runnable(){
            public void run() {
        		KwaakJNI.queueTouchpadEvent(action, x, -y);
            }});
        return true;
	}
	
	public boolean queueTrackballEvent(final int action, final float x, final float y)
	{
		/* Make sure all communication with Quake is done from the Renderer thread */
        queueEvent(new Runnable(){
            public void run() {
        		KwaakJNI.queueTrackballEvent(action, x, y);
            }});
        return true;
	}
	
	private final char DEFAULT_O_BUTTON_LABEL = 0x25CB;   //hex for WHITE_CIRCLE
	
	private boolean isXOkeysSwapped() {
	    boolean flag = false;
	    int[] ids = InputDevice.getDeviceIds();
	    for (int i= 0; ids != null && i<ids.length; i++) {
	        KeyCharacterMap kcm = KeyCharacterMap.load(ids[i]);
	        if ( kcm != null && DEFAULT_O_BUTTON_LABEL ==
	               kcm.getDisplayLabel(KeyEvent.KEYCODE_DPAD_CENTER) ) {
	            flag = true;
	            break;
	        }
	    }
	    return flag;
	}

	public static final int QK_ENTER = 13;
	public static final int QK_ESCAPE = 27;
	public static final int QK_BACKSPACE = 127;
	public static int QK_LEFT = 'a';
	public static int QK_RIGHT = 'd';
	public static final int QK_LEFT_MENU = 134;
	public static final int QK_RIGHT_MENU = 135;
	public static final int QK_UP = 132;
	public static final int QK_DOWN = 133;
	public static final int QK_CTRL = 137;
	public static final int QK_SHIFT = 138;
	public static final int QK_CONSOLE = 340;

	public static final int QK_F1 = 145;
	public static final int QK_F2 = 146;
	public static final int QK_F3 = 147;
	public static final int QK_F4 = 148;
	public static final int XPERIA_PLAY_L1 = 102;	// space
	public static final int XPERIA_PLAY_R1 = 103;	// ctrl
	public static final int XPERIA_PLAY_X = 23;	// enter
	public static final int XPERIA_PLAY_SQUARE = 99;	// [
	public static final int XPERIA_PLAY_O = 4;	// && (AKeyEvent_getMetaState(event) == AMETA_ALT_ON) ]
	public static final int XPERIA_PLAY_START = 108;	// ]
	public static final int XPERIA_PLAY_TRIANGLE = 100;	// c
	
	public int androidKeyCodeToQuake(int aKeyCode, KeyEvent event)
	{	
		/* Convert non-ASCII keys by hand */
		
		/* For now map the focus buttons to F1 and let the user remap it in game.
		 * This should allow some basic movement on the Nexus One if people map it to forward.
		 * At least on the Milestone the camera button itself is shared with the Focus one. You have
		 * to press focus first and then you hit camera, this leads to the following event sequence which
		 * I don't handle right now: focus_down -> camera_down -> camera_up -> focus_up.
		 */
		if (aKeyCode == KeyEvent.KEYCODE_FOCUS){
			return QK_F1;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			return QK_F2;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_VOLUME_UP){
			return QK_F3;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_DPAD_UP){
			return QK_UP;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			return QK_DOWN;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_ENTER){
			return QK_ENTER;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_SEARCH){
			if (System.currentTimeMillis() - lastSearch > backInterval){
				lastSearch = System.currentTimeMillis();
				return QK_CONSOLE;
			}
		}
		else if (aKeyCode == KeyEvent.KEYCODE_DEL){
			return QK_BACKSPACE;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_ALT_LEFT){
			return QK_CTRL;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_SHIFT_LEFT){
			return QK_SHIFT;
		}
		else if (aKeyCode == XPERIA_PLAY_L1){
			return ' ';
		}
		else if (aKeyCode == XPERIA_PLAY_R1){
			return QK_CTRL;
		}
		else if (aKeyCode == XPERIA_PLAY_SQUARE){
			return '[';
		}
		else if (aKeyCode == XPERIA_PLAY_START){
			return QK_ESCAPE;
		}
		else if (aKeyCode == XPERIA_PLAY_TRIANGLE){
			return 'c';
		}
		else if (aKeyCode == KeyEvent.KEYCODE_MENU){
			return -1;
		}
		int result = 0;
		if (inGame){
			result = getInGameKeyBindings(aKeyCode, event);
		}
		else{
			result = getInMenuKeyBindings(aKeyCode, event);
		}
		if (result != 0){
			return result;
		}

		/* Let Android do all the character conversion for us. This way we don't have
		 * to care about modifier keys and specific keyboard layouts.
		 * TODO: add some more filtering
		 */
		int uchar = event.getUnicodeChar();
		if(uchar < 127)
			return uchar;

		return 0;
	}
	
	public int getInGameKeyBindings(int aKeyCode, KeyEvent event){
		if (aKeyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			return QK_LEFT;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			return QK_RIGHT;
		}
		else if (aKeyCode == XPERIA_PLAY_X){
			if (isXOkeysSwapped){
				return ']';
			}
			else{
				return QK_ENTER;
			}
			
		}
		else if (aKeyCode == KeyEvent.KEYCODE_BACK){
			if (isXOkeysSwapped){
				return QK_ENTER;
			}
			else{
				if (System.currentTimeMillis() - lastBack > backInterval){
					lastBack = System.currentTimeMillis();
					if(!event.isAltPressed()){
						return QK_ESCAPE;
					}
					else{
						return ']';
					}
				}
			}
		}
		return 0;
	}
	
	public int getInMenuKeyBindings(int aKeyCode, KeyEvent event){
		if (aKeyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			return QK_LEFT_MENU;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			return QK_RIGHT_MENU;
		}
		else if (aKeyCode == XPERIA_PLAY_X){
			return QK_ENTER;
		}
		else if (aKeyCode == KeyEvent.KEYCODE_BACK){
			if (System.currentTimeMillis() - lastBack > backInterval){
				lastBack = System.currentTimeMillis();
				return QK_ESCAPE;
			}
		}
		return 0;
	}

	public void showKeyboard() {
		mKwaakRenderer.showKeyboard();
	}
}
