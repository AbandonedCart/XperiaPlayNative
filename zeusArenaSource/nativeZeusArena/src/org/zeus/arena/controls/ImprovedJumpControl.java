package org.zeus.arena.controls;

public class ImprovedJumpControl extends JumpControl{
	
	public ImprovedJumpControl(){
		preferenceName = "ImpJump";
		readableName = "Jump";
		blocking = false;
		visable = true;
		readControlPreferences();
	}
}
