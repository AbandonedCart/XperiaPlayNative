package quake.two.controls;

public class ImprovedJumpControl extends JumpControl{
	
	public ImprovedJumpControl(){
		preferenceName = "ImpJump";
		readableName = "Jump";
		blocking = false;
		visable = true;
		readControlPreferences();
	}
}
