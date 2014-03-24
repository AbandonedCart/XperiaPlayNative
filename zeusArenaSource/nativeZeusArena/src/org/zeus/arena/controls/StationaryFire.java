package org.zeus.arena.controls;

import org.zeus.arena.Control;
import org.zeus.arena.KwaakView;
import org.zeus.arena.MyMotionEvent;
import org.zeus.arena.R;
import org.zeus.arena.R.drawable;

import android.content.Context;
import android.widget.ImageView;

public class StationaryFire extends Control{
	
	private int fireDown = 0;
	
	public StationaryFire(){
		preferenceName = "StatFire";
		readableName = "Stationary Fire";
		blocking = true;
		visable = true;
		readControlPreferences();
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		int state = event.getState();
		view.queueKeyEvent(KwaakView.QK_CTRL, state);
		fireDown = state;
		lastTouch = System.currentTimeMillis();
		return true;
	}
	
	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (fireDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_CTRL, 0);
			fireDown = 0;
		}
		return result;
	}
	
	/*@Override
	public boolean hardwareUpdate(int keycode, boolean isDown){
		boolean result = false;
		if (keycode == KwaakView.QK_CTRL){
			lastTouch = System.currentTimeMillis();
			fireDown = isDown;
			result = true;
		}
		return result;
	}*/
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.fire);
		return imageView;
	}

}
