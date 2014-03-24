package org.zeus.arena.controls;

import org.zeus.arena.Control;
import org.zeus.arena.KwaakView;
import org.zeus.arena.MyMotionEvent;
import org.zeus.arena.MyVector;
import org.zeus.arena.Point;

import android.widget.RelativeLayout;

public class XperiaPlayLeftAnalogControl{
	
	private KwaakView view;
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

	public XperiaPlayLeftAnalogControl(KwaakView view){
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
					view.queueKeyEvent(KwaakView.QK_LEFT, state);	//left
					leftDown = state;
					lastLeft = System.currentTimeMillis();
				}
				else{
					view.queueKeyEvent(KwaakView.QK_LEFT_MENU, state);	//left
					lastLeftMenu = System.currentTimeMillis();
					leftDownMenu = state;
				}
				
			}
			else if (isLeft == 0){
				if (view.isInGame()){
					view.queueKeyEvent(KwaakView.QK_RIGHT, state);	//left
					lastRight = System.currentTimeMillis();
					rightDown = state;
				}
				else{
					view.queueKeyEvent(KwaakView.QK_RIGHT_MENU, state);	//left
					lastRightMenu = System.currentTimeMillis();
					rightDownMenu = state;
				}
			}
			else{
				view.queueKeyEvent(KwaakView.QK_LEFT_MENU, 0);
				view.queueKeyEvent(KwaakView.QK_RIGHT_MENU, 0);
				view.queueKeyEvent(KwaakView.QK_LEFT, 0);
				view.queueKeyEvent(KwaakView.QK_RIGHT, 0);
				leftDown = 0;
				rightDown = 0;
				rightDownMenu = 0;
				leftDownMenu = 0;
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

	public boolean killBrokenTouchEvent() {
		boolean result = false;
		if (leftDown != 0 && (System.currentTimeMillis() - lastLeft > Control.interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_LEFT, 0);	//left
			leftDown = 0;
		}
		if (leftDownMenu != 0 && (System.currentTimeMillis() - lastLeftMenu > Control.interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_LEFT_MENU, 0);	//left
			leftDownMenu = 0;
		}
		if (rightDown != 0 && (System.currentTimeMillis() - lastRight> Control.interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_RIGHT, 0);	//left
			rightDown = 0;
		}
		if (rightDownMenu != 0 && (System.currentTimeMillis() - lastRightMenu > Control.interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_RIGHT_MENU, 0);	//left
			rightDownMenu = 0;
		}
		if (upDown != 0 && (System.currentTimeMillis() -lastUp> Control.interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_UP, 0);	//up
			upDown = 0;
		}
		if (downDown != 0 && (System.currentTimeMillis() - lastDown > Control.interval)){
			result = true;
			view.queueKeyEvent(KwaakView.QK_DOWN, 0);	//down
			downDown = 0;
		}
		return result;
	}

}
