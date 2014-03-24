package org.zeus.arena;

import org.zeus.arena.R;


public class Persistence {
	
	private Game game;
	private static Persistence persistence;
	
	private Persistence(Game game){
		this.game = game;
	}
	
	public static void setGame(Game game){
		persistence = new Persistence(game);
	}
	
	public static Persistence getPersistence(){
		return persistence;
	}
	
	public Game getGame(){
		return game;
	}

}
