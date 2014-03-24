package quake.two.controls;

import quake.two.android.*;
import android.view.MotionEvent;

public class XperiaPlayLook {
	
	private QuakeView view;
	private int radius = 180;	// 322/2 or 360/2?
	private Point position;
	float padx_scale = (1.0f/(966/3.0f))/3.0f;
	float pady_scale = (1.0f/360)/3.0f;
	float prevX = 0;
	float prevY = 0;
	private int sensitivityFactor = 20;
	private final int  JITTERTHRESHOLD = 2;
	private boolean swipeModeOn;
	private int action = MotionEvent.ACTION_CANCEL;

	public XperiaPlayLook(QuakeView view){
		this.view = view;
		position = new Point(805, 180);
		swipeModeOn = PlayerPreferences.getThePlayerPreferences().preferenceOn("Swipe");
	}
	
	public boolean touched(MultiMotionEvent event){
		boolean result = false;
		if (distanceFromCenter(event).length < radius){
			if (event.getAction() == MotionEvent.ACTION_MOVE && action == MotionEvent.ACTION_UP){	//if we missed an action down
				action = MotionEvent.ACTION_DOWN;
			}
			else{
				action = event.getAction();
			}
			result = true;
		}
		else{
			action = MotionEvent.ACTION_UP;
		}
		return result;
	}
	
	private MyVector distanceFromCenter(MultiMotionEvent event){
		return position.substract(new Point(event.getX(), event.getY()));
	}
	
	public void touchEvent(MultiMotionEvent event) {
		int action = MotionEvent.ACTION_MASK & event.getAction();
		float x = event.getX();
		float y = event.getY();
		x = (x - 644);
		y = (360 - y);

		if(swipeModeOn){
			x = (view.getWidth() * x * padx_scale);
			y = (view.getHeight() * y * pady_scale);
			processRightAnalog(action, x, y);
		}
		else{
			view.queueTouchpadEvent(action, (x-(161))/sensitivityFactor, (y-(180))/sensitivityFactor, radius*2, radius*2);
		}
	}
	
	public void refreash(){
		swipeModeOn = PlayerPreferences.getThePlayerPreferences().preferenceOn("Swipe");
	}
	
	public void processRightAnalog(int action, float x, float y){
		if (Math.abs(prevX-x) > JITTERTHRESHOLD){
			prevX = x;
		}
		if (Math.abs(prevY-y) > JITTERTHRESHOLD){
			prevY = y;
		}
		view.queueMotionEvent(this.action, prevX-606, -prevY, 1, radius*2, radius*2);
	}
}
