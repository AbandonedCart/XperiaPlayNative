package quake.two.controls;

import java.util.List;

import org.zeus.arena.R;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import quake.two.android.*;

public class ChangeWeaponControl extends Control{
	
	private int weaponDown = 0;
	
	public ChangeWeaponControl(){
		preferenceName = "Weapon";
		readableName = "Next weapon";
		blocking = false;
		visable = true;
		readControlPreferences();
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		int state = event.getState();
		if (System.currentTimeMillis() - lastTouch > interval){
			view.postKeyEvent(']', state);
			weaponDown = state;
		}
		lastTouch = System.currentTimeMillis();
		return true;
	}
 
	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		//if (!weaponDown && (System.currentTimeMillis() - lastTouch > interval)){
		if (weaponDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.postKeyEvent(']', 0);
			weaponDown = 0;
		}
		return result;
	}

	@Override
	public int getRadius() {
		return radius;
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.weapon);
		return imageView;
	}
}
