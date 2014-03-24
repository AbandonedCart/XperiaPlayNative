package org.zeus.arena;

import java.lang.Math;
import org.zeus.arena.R;

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
