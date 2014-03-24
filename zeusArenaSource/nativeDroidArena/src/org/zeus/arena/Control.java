package org.zeus.arena;

import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public abstract class Control {
	
	public final static long interval = 100;
	protected KwaakView view;
	protected int radius;
	protected int touchRadius;
	protected Point position;
	protected long lastTouch = 0;
	protected boolean blocking;
	protected String preferenceName;
	protected String readableName;
	protected ImageView imageView;
	protected boolean visable;
	protected boolean isOn;
	private int alpha;
	
	public void setView(KwaakView view){
		this.view = view;
	}
	
	public RelativeLayout.LayoutParams getLayoutParams(int screenWidth, int screenHeight){
		RelativeLayout.LayoutParams params = null;
		if (visable && isOn){
			params = new RelativeLayout.LayoutParams(radius*2,radius*2);
			params.leftMargin = (int) position.x-radius;
			params.topMargin = (int) position.y-radius;
		}	
		return params;
	}
	
	protected void readControlPreferences(){
		isOn = false;
		List<String> values = PlayerPreferences.getThePlayerPreferences().getPreference(preferenceName);
		if (values.get(0).equals("On")){
			isOn = true;
		}
		if (visable){
			radius = new Integer(values.get(1)).intValue();
			touchRadius = new Integer(values.get(2)).intValue();
			int x = new Integer(values.get(3)).intValue();
			int y = new Integer(values.get(4)).intValue();
			position = new Point(x, y);
			alpha = new Integer(values.get(5)).intValue();
			//Log.d("DROID_ARENA", preferenceName + "= r: " + radius + ", tR: " + touchRadius + ", x: " + x + ", y: " + y + ", alpha:" + alpha + ", visable: " + visable + ", on: " + isOn);	
		}	
	}
	
	public void onStartTouch(){
		
	}
	
	public void onEndTouch(){
		
	}
	
	public boolean isOn(){
		return isOn;
	}
	
	public boolean touchEvent(MyMotionEvent event) {
		lastTouch = System.currentTimeMillis();
		return false;
	}
	
	public boolean touched(MyMotionEvent event){
		boolean result = false;
		if (distanceFromCenter(event).length < touchRadius){
			result = true;
		}
		return result;
	}

	protected MyVector distanceFromCenter(MyMotionEvent event){
		//Log.d("DROID_ARENA", preferenceName + "= r: " + radius + ", tR: " + touchRadius + ", position: " + position);
		return position.substract(new Point(event.getX(), event.getY()));
	}
	
	public boolean killBrokenTouchEvent() {
		return false;
	}
	
	public void setOn(boolean on){
		isOn = on;
		updatePerefernces();
	}
	
	public void setAlpha(int newAlpha){
		alpha = newAlpha;
		updatePerefernces();
	}
	
	public void setPosition(Point point){
		position = point;
		updatePerefernces();
	}
	
	public void setRadius(int newRadius){
		int difference = touchRadius - radius;
		radius = newRadius;
		touchRadius = radius + difference;
		updatePerefernces();
	}
	
	public void updatePerefernces(){
		List<String> values = new Vector<String>();
		if (isOn){
			values.add("" + "On");
		}
		else{
			values.add("" + "Off");
		}
		values.add("" + radius);
		values.add("" + touchRadius);
		values.add("" + (int) position.x);
		values.add("" + (int) position.y);
		values.add("" + alpha);
		PlayerPreferences.getThePlayerPreferences().updatePreference(preferenceName, values);
	}
	
	public int getRadius() {
		return radius;
	}
	
	public int getAlpha(){
		return alpha;
	}
	
	public String getReadableName(){
		return readableName;
	}
	
	public boolean isBlocking(){
		return blocking;
	}
	
	public boolean isVisable(){
		return visable;
	}
	
	public KwaakView getKwaakView(){
		return view;
	}
	
	public ImageView getImageView(Context context){
		if (imageView == null){
			imageView = new ImageView(context);
		}
		imageView.setAlpha(alpha);
		return imageView;
	}

}
