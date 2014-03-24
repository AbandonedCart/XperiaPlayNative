package org.zeus.arena;

import android.util.Log;
import android.view.MotionEvent;
import org.zeus.arena.R;

public class MultiMotionEvent extends MyMotionEvent{
	
	public MultiMotionEvent(MotionEvent event, int index){
		super(event);
		this.i = index;
	}
}
