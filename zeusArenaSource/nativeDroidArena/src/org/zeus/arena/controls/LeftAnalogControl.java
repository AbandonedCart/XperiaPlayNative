package org.zeus.arena.controls;

import org.zeus.arena.Control;
import org.zeus.arena.KwaakView;
import org.zeus.arena.MyMotionEvent;
import org.zeus.arena.MyVector;
import org.zeus.arena.R;
import org.zeus.arena.R.drawable;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LeftAnalogControl extends Control{
	
	private int activationRadius;
	private long lastLeft = 0;
	private long lastRight = 0;
	private long lastUp = 0;
	private long lastDown = 0;
	private int leftDown = 0;
	private int rightDown = 0;
	private int upDown = 0;
	private int downDown = 0;
	private final double activationRadiusScaleFactor = 0.25;
	
	public LeftAnalogControl(){
		preferenceName = "Left";
		readableName = "Move";
		blocking = false;
		visable = true;
		readControlPreferences();
	}
	
	@Override
	protected void readControlPreferences(){
		super.readControlPreferences();
		activationRadius = (int) (radius*activationRadiusScaleFactor);
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		boolean result = false;
		int isLeft = -1;
		int isUp = -1;
		int state = event.getState();
		MyVector distance = distanceFromCenter(event);
		if (distanceFromCenter(event).length < touchRadius){
			if (distance.length >= activationRadius){
				if (distance.x >= activationRadius){
					if (event.getX() < position.x){
						isLeft = 1;
					}
					else{
						isLeft = 0;
					}
				}
				if (distance.y >= activationRadius){
					if (event.getY() < position.y){
						isUp = 1;
					}
					else{
						isUp = 0;
					}
				}
				result = true;
			}
			if (isLeft == 1){
				view.queueKeyEvent(KwaakView.QK_LEFT, state);	//left
				lastLeft = System.currentTimeMillis();
				leftDown = state;
			}
			else if (isLeft == 0){
				view.queueKeyEvent(KwaakView.QK_RIGHT, state);	//right
				lastRight = System.currentTimeMillis();
				rightDown = state;
			}
			else{
				view.queueKeyEvent(KwaakView.QK_LEFT, 0);
				view.queueKeyEvent(KwaakView.QK_RIGHT, 0);
				leftDown = 0;
				rightDown = 0;
			}
			if (isUp == 1){
				view.queueKeyEvent(KwaakView.QK_UP, state);	//up
				lastUp = System.currentTimeMillis();
				upDown = state;
			}
			else if (isUp == 0){
				view.queueKeyEvent(KwaakView.QK_DOWN, state);	//down
				lastDown = System.currentTimeMillis();
				downDown = state;
			}
			else{
				view.queueKeyEvent(KwaakView.QK_UP, 0);
				view.queueKeyEvent(KwaakView.QK_DOWN, 0);
				upDown = 0;
				downDown = 0;
			}
		}
		return result;
	}

	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (leftDown != 0 && (System.currentTimeMillis() - lastLeft > interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_LEFT, 0);	//left
			leftDown = 0;
		}
		if (rightDown != 0 && (System.currentTimeMillis() - lastRight> interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_RIGHT, 0);	//right
			rightDown = 0;
		}
		if (upDown != 0 && (System.currentTimeMillis() -lastUp> interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_UP, 0);	//up
			upDown = 0;
		}
		if (downDown != 0 && (System.currentTimeMillis() - lastDown > interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_DOWN, 0);	//down
			downDown = 0;
		}
		return result;
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.move);
		return imageView;
	}
}
