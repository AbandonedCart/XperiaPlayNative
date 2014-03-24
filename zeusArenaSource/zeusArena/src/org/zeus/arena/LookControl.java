package org.zeus.arena;

import org.zeus.arena.R;

public class LookControl extends Control{
	
	public LookControl(){
		preferenceName = "Look";
		readableName = "Look";
		blocking = false;
		visable = false;
		readControlPreferences();
	}
	
	@Override
	public boolean touchEvent(MyMotionEvent event) {
		return view.queueMotionEvent(event.getAction(), event.getX(), event.getY(), event.getPressure());
	}
	
	public boolean touched(MyMotionEvent event){
		return true;
	}
}
