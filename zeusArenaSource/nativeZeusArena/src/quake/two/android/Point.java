package quake.two.android;

import java.lang.Math;

public class Point {
	
	public float x;
	public float y;
	
	public Point(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public MyVector substract(Point point){
		return new MyVector(Math.abs(x - point.x), Math.abs(y - point.y));
	}

}
