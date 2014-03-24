package quake.two.controls;

import quake.two.android.*;

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
		return view.queueMotionEvent(event.getAction(), event.getX(), event.getY(), event.getPressure(), view.getWidth(), view.getHeight());
	}
	
	public boolean touched(MyMotionEvent event){
		return true;
	}
}
