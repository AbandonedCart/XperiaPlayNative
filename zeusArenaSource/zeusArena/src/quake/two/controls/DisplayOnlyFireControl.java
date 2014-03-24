package quake.two.controls;


import quake.two.android.*;

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
