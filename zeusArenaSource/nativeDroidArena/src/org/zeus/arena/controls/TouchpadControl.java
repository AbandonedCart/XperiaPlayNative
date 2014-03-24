package org.zeus.arena.controls;
import org.zeus.arena.KwaakView;
import org.zeus.arena.MultiMotionEvent;
import org.zeus.arena.MyMotionEvent;

import android.view.MotionEvent;

public class TouchpadControl {
	
	private KwaakView view;
	private XperiaPlayLeftAnalogControl leftAnalog;
	private XperiaPlayLook look;
	
	public TouchpadControl(KwaakView view){
		this.view = view;
		leftAnalog = new XperiaPlayLeftAnalogControl(view);
		look = new XperiaPlayLook(view);
	}
	
	public void refreash(){
		look.refreash();
	}
	
	public void touched(MultiMotionEvent event){		
		if (leftAnalog.touched(event)){
			leftAnalog.touchEvent(event);
		}
		else if (look.touched(event)){
			look.touchEvent(event);
		}
	}
	
	public boolean killBrokenTouchEvent() {
		return leftAnalog.killBrokenTouchEvent();
	}

}
