package org.zeus.arena.controls;

import org.zeus.arena.Control;
import org.zeus.arena.MyMotionEvent;

import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class ImprovedLookControl extends Control{
	
	private int halfScreenWidth;
	private int action = MotionEvent.ACTION_CANCEL;
	private boolean wasTouched = false;
	private boolean touchDown = false;
	
	public ImprovedLookControl(int screenWidth){
		preferenceName = "Look";
		readableName = "Look";
		blocking = false;
		halfScreenWidth = screenWidth/2;
		visable = false;
		readControlPreferences();
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		return view.queueMotionEvent(action, event.getX(), event.getY(), event.getPressure());
	}
	
	@Override
	public void onEndTouch(){
		if (!wasTouched){
			touchDown = false;
			action = MotionEvent.ACTION_UP;
		}
		wasTouched = false;
	}
	
	public boolean touched(MyMotionEvent event){
		boolean result = false;
		if (event.getX() > halfScreenWidth){
			wasTouched = true;
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				touchDown = true;
				action = MotionEvent.ACTION_DOWN;
			}
			else if (event.getAction() == MotionEvent.ACTION_UP){
				touchDown = false;
				action = MotionEvent.ACTION_UP;
			}
			else if (event.getAction() == MotionEvent.ACTION_MOVE && !touchDown){	//if we missed an action down
				touchDown = true;
				action = MotionEvent.ACTION_DOWN;
			}
			else{
				action = event.getAction();
			}
			result = true;
		}
		return result;
	}

	@Override
	public boolean killBrokenTouchEvent() {
		return false;
	}

}
