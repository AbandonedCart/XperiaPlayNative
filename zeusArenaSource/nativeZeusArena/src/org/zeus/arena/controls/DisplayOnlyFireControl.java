package org.zeus.arena.controls;

import org.zeus.arena.MyMotionEvent;
import org.zeus.arena.Point;

public class DisplayOnlyFireControl extends FireControl{
	
	@Override
	public boolean touched(MyMotionEvent event){
		//isActive = false;
		return super.touched(event);
	}
	
	public void setActivePosition(Point point){
		activePosition = position;
	}

}
