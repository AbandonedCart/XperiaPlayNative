package org.zeus.arena;

import org.zeus.arena.R;

public class ImprovedJumpControl extends JumpControl{
	
	public ImprovedJumpControl(){
		preferenceName = "ImpJump";
		readableName = "Jump";
		blocking = false;
		visable = true;
		readControlPreferences();
	}
}
