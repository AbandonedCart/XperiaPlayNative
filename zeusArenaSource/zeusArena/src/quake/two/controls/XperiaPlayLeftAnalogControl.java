package quake.two.controls;

import quake.two.android.*;
import android.widget.RelativeLayout;

public class XperiaPlayLeftAnalogControl{
	
	private QuakeView view;
	private int radius = 180;	// 322/2 or 360/2?
	private final double activationRadiusScaleFactor = 0.25;
	private int activationRadius = (int) (radius*activationRadiusScaleFactor);
	private Point position;
	private long lastLeft = 0;
	private long lastRight = 0;
	private long lastLeftMenu = 0;
	private long lastRightMenu = 0;
	private long lastUp = 0;
	private long lastDown = 0;
	private int leftDownMenu = 0;
	private int rightDownMenu = 0;
	private int leftDown = 0;
	private int rightDown = 0;
	private int upDown = 0;
	private int downDown = 0;

	public XperiaPlayLeftAnalogControl(QuakeView view){
		this.view = view;
		position = new Point(161, 180);
	}
	
	public boolean touched(MyMotionEvent event){
		boolean result = false;
		if (distanceFromCenter(event).length < radius){
			result = true;
		}
		return result;
	}
	
	private MyVector distanceFromCenter(MyMotionEvent event){
		return position.substract(new Point(event.getX(), event.getY()));
	}

	public boolean touchEvent(MyMotionEvent event) {
		boolean result = false;
		int isLeft = -1;
		int isUp = -1;
		int state = event.getState();
		MyVector distance = distanceFromCenter(event);
		if (distanceFromCenter(event).length < radius){
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
				if (view.isInGame()){
					view.postKeyEvent(QuakeView.K_LEFTARROW, state);	//left
					leftDown = state;
					lastLeft = System.currentTimeMillis();
				}
				else{
					view.postKeyEvent(QuakeView.K_LEFTARROW, state);	//left
					lastLeftMenu = System.currentTimeMillis();
					leftDownMenu = state;
				}
				
			}
			else if (isLeft == 0){
				if (view.isInGame()){
					view.postKeyEvent(QuakeView.K_RIGHTARROW, state);	//left
					lastRight = System.currentTimeMillis();
					rightDown = state;
				}
				else{
					view.postKeyEvent(QuakeView.K_RIGHTARROW, state);	//left
					lastRightMenu = System.currentTimeMillis();
					rightDownMenu = state;
				}
			}
			else{
				view.postKeyEvent(QuakeView.K_LEFTARROW, 0);
				view.postKeyEvent(QuakeView.K_RIGHTARROW, 0);
				view.postKeyEvent(QuakeView.K_LEFTARROW, 0);
				view.postKeyEvent(QuakeView.K_RIGHTARROW, 0);
				leftDown = 0;
				rightDown = 0;
				rightDownMenu = 0;
				leftDownMenu = 0;
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

	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (leftDown != 0 && (System.currentTimeMillis() - lastLeft > Control.interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_LEFTARROW, 0);	//left
			leftDown = 0;
		}
		if (leftDownMenu != 0 && (System.currentTimeMillis() - lastLeftMenu > Control.interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_LEFTARROW, 0);	//left menu
			leftDownMenu = 0;
		}
		if (rightDown != 0 && (System.currentTimeMillis() - lastRight> Control.interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_RIGHTARROW, 0);	//right
			rightDown = 0;
		}
		if (rightDownMenu != 0 && (System.currentTimeMillis() - lastRightMenu > Control.interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_RIGHTARROW, 0);	//right menu
			rightDownMenu = 0;
		}
		if (upDown != 0 && (System.currentTimeMillis() -lastUp> Control.interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_UPARROW, 0);	//up
			upDown = 0;
		}
		if (downDown != 0 && (System.currentTimeMillis() - lastDown > Control.interval)){
			result = true;
			view.postKeyEvent(QuakeView.K_DOWNARROW, 0);	//down
			downDown = 0;
		}
		return result;
	}

}
