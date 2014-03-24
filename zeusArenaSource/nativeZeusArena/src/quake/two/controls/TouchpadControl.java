package quake.two.controls;

import quake.two.android.*;
import android.view.MotionEvent;

public class TouchpadControl {
	
	private QuakeView view;
	private XperiaPlayLeftAnalogControl leftAnalog;
	private XperiaPlayLook look;
	
	public TouchpadControl(QuakeView view){
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
