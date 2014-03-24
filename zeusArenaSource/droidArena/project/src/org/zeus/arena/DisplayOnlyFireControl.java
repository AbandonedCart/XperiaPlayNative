package org.zeus.arena;

import org.zeus.arena.R;

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
