package org.zeus.arena;

import java.lang.Math;
import org.zeus.arena.R;

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
