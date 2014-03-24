package org.zeus.arena;
import android.view.MotionEvent;


public class MyMotionEvent {
	
	public MotionEvent ev;
	public int i;
	
	public MyMotionEvent(MotionEvent event){
		this.ev = event;
		i = 0;
	}
	
	public Point getPoint(){
		return new Point(ev.getX(i), ev.getY(i));
	}
	
	public float getX(){
		return ev.getX(i);
	}
	
	public float getY(){
		return ev.getY(i);
	}
	
	public int getAction(){
		return ev.getAction();
	}

	public float getPressure(){
		return ev.getPressure(i);
	}
	
	public boolean actionApplies(int action){
		boolean result = false;
		if ((getAction() & MotionEvent.ACTION_MASK) == action){
			result = true;
		}
		return result;
	}
	
	public boolean actionPointerApplies(int action){
		boolean result = false;
		if ((getAction() & MotionEvent.ACTION_MASK) == (action)){
			final int pointerIndex = (getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (pointerIndex == i){
				result = true;
			}
		}
		return result;
	}
	
	public int getState(){
		int state = -1;
		if (actionPointerApplies(MotionEvent.ACTION_POINTER_DOWN)){
			state  = 1;
		}
		else if (actionPointerApplies(MotionEvent.ACTION_POINTER_UP)){
			state = 0;
		}
		else if(actionApplies(MotionEvent.ACTION_DOWN)){
			state = 1;
		}
		else if(actionApplies(MotionEvent.ACTION_UP)){
			state = 0;
		}
		else if (actionApplies(MotionEvent.ACTION_MOVE)){
			state = 1;
		}
		else if (actionApplies(MotionEvent.ACTION_CANCEL)){
			state = 0;
		}
		return state;
	}
}
