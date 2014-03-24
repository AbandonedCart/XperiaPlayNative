package org.zeus.arena;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import org.zeus.arena.R;

public class JumpControl extends Control{
	
	private int jumpDown = 0;
	
	public JumpControl(){
		preferenceName = "Jump";
		readableName = "Jump";
		blocking = false;
		visable = true;
		readControlPreferences();
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		boolean result = false;
		if (distanceFromCenter(event).length < touchRadius){
			int state = event.getState();
			lastTouch = System.currentTimeMillis();
			view.queueKeyEvent(' ', state);
			jumpDown = state;
			result = true;
		}
		return result;
	}

	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (jumpDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.queueKeyEvent(' ', 0);
			jumpDown = 0;
		}
		return result;
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.jump);
		return imageView;
	}
}
