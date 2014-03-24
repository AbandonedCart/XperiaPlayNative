package quake.two.android;

import java.lang.Math;

public class MyVector {
	
	public float x;
	public float y;
	public float length;
	
	public MyVector(float x, float y){
		this.x = x;
		this.y = y;
		this.length = (float) Math.hypot(x, y);
	}

}
