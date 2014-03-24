package quake.two.android;

public class Persistence {
	
	private Quake2 game;
	private static Persistence persistence;
	
	private Persistence(Quake2 game){
		this.game = game;
	}
	
	public static void setGame(Quake2 game){
		persistence = new Persistence(game);
	}
	
	public static Persistence getPersistence(){
		return persistence;
	}
	
	public Quake2 getGame(){
		return game;
	}

}
