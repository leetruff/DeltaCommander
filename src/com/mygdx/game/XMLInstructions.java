package com.mygdx.game;

public class XMLInstructions {

	public final int health;
	public final int lives;
	public final int ammo;
	public final int score;
	public final int laserAmmo;
	public final int tripleAmmo;
	public final String id;
	
	public XMLInstructions(int health, int lives, int ammo, int score, int laserammo, int tripleammo, String id){
		this.health = health;
		this.lives = lives;
		this.ammo = ammo;
		this.score = score;
		this.laserAmmo = laserammo;
		this.tripleAmmo = tripleammo;
		this.id = id;
	}
	
	public XMLInstructions(){
		this.health = 0;
		this.lives = 0;
		this.ammo = 0;
		this.score = 0;
		this.laserAmmo = 0;
		this.tripleAmmo = 0;
		this.id = "NONE";
	}
}
