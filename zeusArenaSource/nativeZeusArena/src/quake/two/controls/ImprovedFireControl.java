package quake.two.controls;

import java.util.List;

import org.zeus.arena.R;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import quake.two.android.*;

public class ImprovedFireControl extends Control{
	
	private int nullRadius;
	private int vibrateZoneSize = 12;
	private long virationLength = 10;	//4
	private long lastVibrate = 0;
	private long vibrateDelay = 30;
	private Vibrator vibrator;
	private int activationRadius;
	private long lastLeft = 0;
	private long lastRight = 0;
	private long lastUp = 0;
	private long lastDown = 0;
	private int leftDown = 0;
	private int rightDown = 0;
	private int upDown = 0;
	private int downDown = 0;
	private int fireDown = 0;
	private final double activationRadiusScaleFactor = 0.25;
	
	public ImprovedFireControl(Vibrator vibrator){
		this.vibrator = vibrator;
		preferenceName = "ImpFire";
		readableName = "Move/Fire";
		blocking = true;
		visable = true;
		readControlPreferences();
	}
	
	@Override
	protected void readControlPreferences(){
		super.readControlPreferences();
		if (isOn){
			nullRadius = radius - (radius/3);
			activationRadius = (int) (radius*activationRadiusScaleFactor);
		}
	}

	@Override
	public boolean touchEvent(MyMotionEvent event) {
		fire(event);
		boolean result = move(event);
		return result;
	}
	
	public void fire(MyMotionEvent event){
		int state = event.getState();
		if (distanceFromCenter(event).length > nullRadius){
			view.postKeyEvent(QuakeView.K_CTRL, state);
			lastTouch = System.currentTimeMillis();
			fireDown = state;
		}
		else if (distanceFromCenter(event).length > (nullRadius-vibrateZoneSize)){
			/*if ((System.currentTimeMillis() - lastVibrate) > vibrateDelay){
			vibrator.vibrate(virationLength);
			lastVibrate = System.currentTimeMillis();
		}*/
			if (vibrator != null){
				vibrator.vibrate(virationLength);
			}
		}
	}
	
	public boolean move(MyMotionEvent event){
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
				view.postKeyEvent(QuakeView.K_LEFTARROW, state);	//left
				lastLeft = System.currentTimeMillis();
				leftDown = state;
			}
			else if (isLeft == 0){
				view.postKeyEvent(QuakeView.K_RIGHTARROW, state);	//right
				lastRight = System.currentTimeMillis();
				rightDown = state;
			}
			else{
				view.postKeyEvent(QuakeView.K_LEFTARROW, 0);
				view.postKeyEvent(QuakeView.K_RIGHTARROW, 0);
				leftDown = 0;
				rightDown = 0;
			}
			if (isUp == 1){
				view.postKeyEvent(QuakeView.K_UPARROW, state);	//up
				lastUp = System.currentTimeMillis();
				upDown = state;
			}
			else if (isUp == 0){
				view.postKeyEvent(QuakeView.K_DOWNARROW, state);	//down
				lastDown = System.currentTimeMillis();
				downDown = state;
			}
			else{
				view.postKeyEvent(QuakeView.K_UPARROW, 0);
				view.postKeyEvent(QuakeView.K_DOWNARROW, 0);
				upDown = 0;
				downDown = 0;
			}
		}
		return result;
	}
	
	@Override
	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (fireDown != 0 && (System.currentTimeMillis() - lastTouch > interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_CTRL, 0);
			fireDown = 0;
		}
		if (leftDown != 0 && (System.currentTimeMillis() - lastLeft > interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_LEFTARROW, 0);	//left
			leftDown = 0;
		}
		if (rightDown != 0 && (System.currentTimeMillis() - lastRight> interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_RIGHTARROW, 0);	//right
			rightDown = 0;
		}
		if (upDown != 0 && (System.currentTimeMillis() -lastUp> interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_UPARROW, 0);	//up
			upDown = 0;
		}
		if (downDown != 0 && (System.currentTimeMillis() - lastDown > interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_DOWNARROW, 0);	//down
			downDown = 0;
		}
		return result;
	}
	
	@Override
	public ImageView getImageView(Context context){
		super.getImageView(context);	//will set view automatically, no need to use return value
		imageView.setImageResource(R.drawable.movefire);
		return imageView;
	}
}
