package quake.two.controls;

import org.zeus.arena.R;

import android.content.Context;
import android.widget.ImageView;
import quake.two.android.*;


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
		view.postKeyEvent(QuakeView.K_CTRL, state);
		fireDown = state;
		lastTouch = System.currentTimeMillis();
		return true;
	}
	
	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (fireDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_CTRL, 0);
			fireDown = 0;
		}
		return result;
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.fire);
		return imageView;
	}

}
