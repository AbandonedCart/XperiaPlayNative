package org.zeus.arena.controls;

import org.zeus.arena.Control;
import org.zeus.arena.KwaakView;
import org.zeus.arena.MyMotionEvent;
import org.zeus.arena.MyVector;
import org.zeus.arena.Point;
import org.zeus.arena.R;
import org.zeus.arena.R.drawable;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FireControl extends Control{	

	protected int fireDown = 0;
	protected Point activePosition;
	
	public FireControl(){
		preferenceName = "Fire";
		readableName = "Fire";
		blocking = false;
		visable = true;
		readControlPreferences();
		setActivePosition(position);
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		int state = event.getState();
		if (state != 1){
			resetActivePostion();
		}
		else{
			setActivePosition(new Point(event.getX(), event.getY()));
		}
		view.queueKeyEvent(KwaakView.QK_CTRL, state);
		fireDown = state;
		lastTouch = System.currentTimeMillis();
		return true;
	}
	
	protected MyVector distanceFromCenter(MyMotionEvent event){
		return activePosition.substract(new Point(event.getX(), event.getY()));
	}
	
	public void setActivePosition(Point point){
		activePosition = point;
	}
	
	public void setPosition(Point point){
		position = point;
		setActivePosition(position);
		updatePerefernces();
	}
	
	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (fireDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_CTRL, 0);
			fireDown = 0;
			resetActivePostion();
		}
		return result;
	}
	
	public void resetActivePostion(){
		setActivePosition(position);
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.fire);
		return imageView;
	}
}
