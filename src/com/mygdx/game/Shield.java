package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.GameScreen;
import com.mygdx.game.MyGdxGame;

/**
 * Schild, das Hero fuer kurze Zeit vor gegnerischen Projektilen beschuetzt. <br>
 * Wird aktiviert, wenn der Spieler ein Shield-PowerUp einsammelt.
 * @author Tim
 */
public class Shield extends Sprite  {

	static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/playerShield.png"));
	
    boolean active;
    float timeLeft;
	
    /**
     * Konstruktor fuer die Erstellung eines Shield Objekts
     * @param hero Hero (Spielercharakter) um das Schild an dessen Position platzieren und zeichnen zu koennen
     */
	public Shield(Hero hero){
		
		super(defaultTexture);
			
		setBounds(hero.getX(), hero.getY(), 50 / MyGdxGame.PPM, 50 / MyGdxGame.PPM);
		
		active = false;
		timeLeft = 0;
	}
	
	/**
	 * Position fuer das Zeichnen des Sprites an die Position des Heros anpassen.<br>
	 * Wenn Schild aktiv ist, restliche Zeit verkuerzen.
	 */
	public void update(float delta, Hero hero){	
		
		setPosition(hero.getX(),hero.getY());

		if(active)
		{
			setTimeLeft(getTimeLeft()-delta);
		}
	}
	
	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public float getTimeLeft() {
		return timeLeft;
	}


	public void setTimeLeft(float timeLeft) {
		this.timeLeft = timeLeft;
		
		if(this.timeLeft <= 0)
		{
			this.timeLeft = 0;
			this.active = false;
		}
	}
	
	public void activate()
	{
		this.active = true;
		this.timeLeft = 10;
	}
	
	public void deactivate()
	{
		this.active = false;
		this.timeLeft = 0;
	}
	

}


