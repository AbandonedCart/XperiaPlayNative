package org.zeus.arena;

import java.util.List;
import java.util.Vector;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MotionEvent.PointerCoords;

public class NativeMotionEvent {
	
	private int action;
	private List<PointerCoords> pointerCoords;
	private float xPrecision;
	private float yPrecision;
	private int deviceId;
	private int edgeFlags;
	private int source;
	private int flags;
    // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
	private int metaState;
	private boolean touchPadEvent;
	
	public NativeMotionEvent(boolean touchPadEvent){
		pointerCoords = new Vector<PointerCoords>();
		edgeFlags = 0;
		xPrecision = 1;
		yPrecision = 1;
		flags = 0;
		metaState = 0;
		this.touchPadEvent = touchPadEvent;
	}
	
	public void addPointer(int action, int x, int y, int source, int device_id){
		this.action = action;
		PointerCoords coords = new PointerCoords();
		coords.x = x;
		if (touchPadEvent){
			coords.y = 366-y;
		}
		else{
			coords.y = y;
		}
		pointerCoords.add(coords);
		this.source = source;
		deviceId = device_id;
	}
	
	public void dispatchMotionEvent(KwaakView view){
		long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        MotionEvent motionEvent = MotionEvent.obtain(
            downTime, eventTime, action, pointerCoords.size(),
            getPointerIds(), getPointerCoords(), metaState, 
            xPrecision, yPrecision, deviceId, 
            edgeFlags, source, flags);
        if (touchPadEvent){
        	view.onTouchPadEvent(motionEvent);
        }
        else{
        	view.dispatchTouchEvent(motionEvent);
        }
	}
	
	private PointerCoords[] getPointerCoords(){
		PointerCoords[] result = new PointerCoords[pointerCoords.size()];
		int i = 0;
		for (PointerCoords coords : pointerCoords){
			result[i] = coords;
			i++;
		}
		return result;
	}
	
	private int[] getPointerIds(){
		int[] pointerIds = new int[pointerCoords.size()];
		for (int i = 0; i < pointerCoords.size(); i++){
			pointerIds[i] = i;
		}
		return pointerIds;
	}

}
