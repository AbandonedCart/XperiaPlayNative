package quake.two.controls;

import org.zeus.arena.R;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import quake.two.android.*;

public class YControl extends Control{
	
	private int enterDown = 0;
	
	public YControl(){
		preferenceName = "Y";
		readableName = "Y";
		blocking = false;
		visable = true;
		readControlPreferences();
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		int state = event.getState();
		view.postKeyEvent('y', state);
		enterDown = state;
		lastTouch = System.currentTimeMillis();
		return true;
	}

	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (enterDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.postKeyEvent('y', 0);
			enterDown = 0;
		}
		return result;
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.y);
		return imageView;
	}
}
