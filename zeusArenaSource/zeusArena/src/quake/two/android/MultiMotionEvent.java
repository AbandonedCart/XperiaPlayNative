package quake.two.android;

import android.util.Log;
import android.view.MotionEvent;

public class MultiMotionEvent extends MyMotionEvent{
	
	public MultiMotionEvent(MotionEvent event, int index){
		super(event);
		this.i = index;
	}
}
