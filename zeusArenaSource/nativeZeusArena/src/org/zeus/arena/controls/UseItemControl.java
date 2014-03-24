package org.zeus.arena.controls;

import org.zeus.arena.Control;
import org.zeus.arena.KwaakView;
import org.zeus.arena.MyMotionEvent;
import org.zeus.arena.R;
import org.zeus.arena.R.drawable;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class UseItemControl extends Control{
	
	private int enterDown = 0;
	
	public UseItemControl(){
		preferenceName = "Item";
		readableName = "Use item";
		blocking = false;
		visable = true;
		readControlPreferences();
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		int state = event.getState();
		view.queueKeyEvent(KwaakView.QK_ENTER, state);
		enterDown = state;
		lastTouch = System.currentTimeMillis();
		return true;
	}

	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (enterDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_ENTER, 0);
			enterDown = 0;
		}
		return result;
	}
	
	/*@Override
	public boolean hardwareUpdate(int keycode, boolean isDown){
		boolean result = false;
		if (keycode == KwaakView.QK_ENTER){
			enterDown = isDown;
			lastTouch = System.currentTimeMillis();
			result = true;
		}
		return result;
	}*/
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.item);
		return imageView;
	}
}
