package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryonet.Connection;

import network.ShootingMessage;

/**
 * Spielercharakter, der vom Spieler durch die Level von Delta Commander gesteuert wird.
 * Kann mit verschiedenen Projektilen schiessen, <br>
 * verfuegt ueber mehrere Animationen, <br>
 * speichert Leben, Lebenspunkte, Munition und Punktzahl.
 * 
 * @author Lars, Tim
 */
public class Hero extends Sprite {

	//Wie viele Bullets duerfen gleichzeitig existieren
	private static final int bulletLimit = 30;
	
	Texture texture;
	Sprite sprite;
	Rectangle rectangle;
	
	MyGdxGame game;
	
	//Box2D
	World world;
	public Body b2body;
	FixtureDef fdef;
	Fixture fixture;
	
	//Animationen
	public enum State {FALLING, JUMPING, STANDING, RUNNING, PLATTFORM_STAND, PLATTFORM_WALK, RUNNING_SHOOTING};
	public State currentState, previousState;
	
	float stateTimer;
	boolean runningRight;
	TextureAtlas atlas;
	
	Animation heroRun;
	static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/hero_stand.png"));
	TextureRegion defaultTextureRegion;
	TextureRegion jumpTextureRegion;
	
	//Projektile
	ArrayList<Bullet> bulletList;
	ArrayList<Laserbullet> laserbulletList;
	
    float timePassed;
    float lastShot;
    int nextRemoved;
	float invulnerabilityTime;
    int health;
    int lives;
    int ammo;
    int score;
    int savedScore;
    
    int laserammo = 0;
    int tripleammo = 0;
    
	//Hero auf Platform für Animationen
	boolean standingOnPlatform = false;
	boolean walkingOnPlatform = false;
    
    GameScreen screen;
    MovingPlatform movingPlatform;
    
    OrthographicCamera gamecam;
	
    /**
     * Konstruktor fuer die Erstellung des Hero beim Laden eines Spielstandes / Uebergang ins naechste Level
     * @param world Box2D Welt in die Hero platziert wird
     * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen (zB Sounds)
     * @param cam  GameCam, die auf Hero fixiert ist
     * @param health Verbleibende Lebenspunkte
     * @param lives Anzahl verbleibender Leben
     * @param ammo Verbleibende Munition
     * @param score Bisher erreichte Punktzahl
     * @param laserAmmo Verbleibende Laser-Munition
     * @param tripleAmmo Verbleibende Triple-Munition
     */
    public Hero(World world, MyGdxGame game, OrthographicCamera cam, int health, int lives, int ammo, int score, int laserAmmo, int tripleAmmo){
    	
    	this(world, game, cam);
    	
    	this.health = health;
    	this.lives = lives;
    	this.ammo = ammo;
    	this.score = score;
    	this.laserammo = laserAmmo;
    	this.tripleammo = tripleAmmo;
    	this.savedScore = score;
    	
    }
    
    /**
     * Haupt-Konstruktor fuer die Erstellung eines Heros. <br>
     * Setzt Attribute und definiert Animations-Frames.
     * @param world Box2D Welt in die Hero platziert wird
     * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen (zB Sounds)
     * @param cam GameCam, die auf Hero fixiert ist
     */
	public Hero(World world, MyGdxGame game, OrthographicCamera cam){
		
		super(defaultTexture);
		
		this.game = game;
		
		this.world = world;
		defineHero();
		
		currentState = State.STANDING;
		previousState = State.STANDING;
		stateTimer = 0;
		runningRight = true;
		
		atlas = new TextureAtlas(Gdx.files.internal("sprites/runanimation.pack"));

		//Run Animation Frames
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion("run1").getTexture(), 1, 1, 50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run2").getTexture(), 53, 1, 50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run3").getTexture(), 105, 1, 50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run4").getTexture(), 157, 1, 50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run5").getTexture(), 209, 1,  50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run6").getTexture(), 261, 1, 50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run7").getTexture(), 313, 1, 50, 50));
		frames.add(new TextureRegion(atlas.findRegion("run8").getTexture(), 365, 1, 50, 50));


		heroRun = new Animation(0.08f, frames);
		frames.clear();

		//Textureregion Jump
		jumpTextureRegion = new TextureRegion(new Texture(Gdx.files.internal("sprites/hero_jump.png")));
		defaultTextureRegion = new TextureRegion(defaultTexture);
		
		setBounds(0, 0, 50 / MyGdxGame.PPM, 50 / MyGdxGame.PPM);
		this.setPosition(0, 0);
		
		//Startposition fuer Hauptmenu-Screen
		b2body.setTransform(640 / MyGdxGame.PPM, 100 / MyGdxGame.PPM, 0);
		b2body.setLinearVelocity(0f, 0f);
		
		invulnerabilityTime = 0;
		
		bulletList = new ArrayList<Bullet>();
		laserbulletList = new ArrayList<Laserbullet>();
		this.health = 100;
		this.lives = 3;
		this.ammo = 0;
		this.score = 0;
		this.nextRemoved = 1;
		this.savedScore = 0;
		
		gamecam = cam;
	}
	
	/**
	 * Alternativer kKonstruktor fuer die Erstellung des Hero beim Laden eines Spielstandes / Uebergang ins naechste Level
	 * @param world Box2D Welt in die Hero platziert wird
	 * @param screen Level-Screen
	 * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen (zB Sounds)
	 * @param cam GameCam, die auf Hero fixiert ist
	 * @param health Verbleibende Lebenspunkte
     * @param lives Anzahl verbleibender Leben
     * @param ammo Verbleibende Munition
     * @param score Bisher erreichte Punktzahl
     * @param laserAmmo Verbleibende Laser-Munition
     * @param tripleAmmo Verbleibende Triple-Munition
	 */
	public Hero(World world, GameScreen screen, MyGdxGame game, OrthographicCamera cam, int health, int lives, int ammo, int score, int laserAmmo, int tripleAmmo){
		this(world, screen, game, cam);
		this.health = health;
    	this.lives = lives;
    	this.ammo = ammo;
    	this.score = score;
    	this.laserammo = laserAmmo;
    	this.tripleammo = tripleAmmo;
    	this.savedScore = score;
	}
	
	/**
	 * Alternativer Konstruktor, der StartPosition fuer einige Level anpasst
	 * @param world Box2D Welt in die Hero platziert wird
	 * @param screen Level-Screen
	 * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen (zB Sounds)
	 * @param cam GameCam, die auf Hero fixiert ist
	 */
	public Hero(World world, GameScreen screen, MyGdxGame game, OrthographicCamera cam) {
		
		this(world, game, cam);
		this.screen = screen;	
		
		//Startposition fuer Game-Screen
		b2body.setTransform(150 / MyGdxGame.PPM, 100 / MyGdxGame.PPM, 0);
		b2body.setLinearVelocity(0f, 0f);
	}
	public Hero(){}

	
	/**
	 * Hero in jedem render Schritt aktualisieren.
	 * Position von Sprite + Bullets, sowie Unverwundbarkeitszeit anpassen.
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void update(float delta){
		
		//Position aller Projektile des Heros updaten
		for(int i = 0; i < bulletList.size(); i++){
			bulletList.get(i).update(delta);
		}
		
		for(int i = 0; i < laserbulletList.size(); i++){
			laserbulletList.get(i).update(delta);
		}
		
		//Unverwundbarkeitszeit verkuerzen
		if(this.isInvulnerable()){
			this.setInvulnerabilityTime(this.getInvulnerabilityTime()-delta);
		}
		
		//Position des Heros aktualisieren und Sprite an aktuellen Status anpassen (Animation)
		setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
		setRegion(getFrame(delta));
		
	}
	
	/**
	 * Berechnet den aktuellen Frame der Animation fuer den Sprite des Heros aus Status und statetime.
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 * @return Aktueller Frame der aktuellen Animation 
	 */
	public TextureRegion getFrame(float delta){
		currentState = getState();
		
		TextureRegion region;
		
		switch(currentState){
		case JUMPING:
			region = jumpTextureRegion; break;
		case RUNNING:
			region = heroRun.getKeyFrame(stateTimer, true); break;
		case RUNNING_SHOOTING:
			region = heroRun.getKeyFrame(stateTimer, true); break;
		case PLATTFORM_STAND:
			region = defaultTextureRegion; break;
		case PLATTFORM_WALK:
			region = heroRun.getKeyFrame(stateTimer, true); break;
		case FALLING:
		case STANDING:
		default:
			region = defaultTextureRegion; break;
		}
		//Auf Plattform nach links laufen
		if(currentState == State.PLATTFORM_WALK && b2body.getLinearVelocity().x - movingPlatform.body.getLinearVelocity().x < 0 && !region.isFlipX()){
			region.flip(true, false);
			runningRight = false;
		}
		//Auf Plattform nach rechts laufen
		else if(currentState == State.PLATTFORM_WALK && b2body.getLinearVelocity().x - movingPlatform.body.getLinearVelocity().x > 0 && region.isFlipX()){
			region.flip(true, false);
			runningRight = true;
		}
		//Schiessen waehrend dem Laufen
		else if(currentState == State.RUNNING_SHOOTING && gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).x < b2body.getPosition().x && !region.isFlipX()){
			region.flip(true, false);
			runningRight = false;
		}
		//Schiessen waehrend dem Laufen
		else if(currentState == State.RUNNING_SHOOTING && gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).x > b2body.getPosition().x && region.isFlipX()){
			region.flip(true, false);
			runningRight = true;
		}
		//Nach links laufen
		else if(currentState != State.PLATTFORM_WALK && currentState != State.RUNNING_SHOOTING && (b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()){
				region.flip(true, false);
				runningRight = false;
		}
		//Nach rechts laufen
		else if(currentState != State.PLATTFORM_WALK && currentState != State.RUNNING_SHOOTING && (b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()){
				region.flip(true, false);
				runningRight = true;
		}
		
		
		
		stateTimer = currentState == previousState ? stateTimer + delta : 0;
		previousState = currentState;
		return region;
	}

	/**
	 * Berechnet aktuellen Status, in dem sich Hero befindet und gibt diesen zurueck.
	 * @return Aktueller Status des Heros
	 */
	public State getState(){
		if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING))
			return State.JUMPING;
		else if(walkingOnPlatform)
			return State.PLATTFORM_WALK;
		else if(standingOnPlatform)
			return State.PLATTFORM_STAND;
		else if(b2body.getLinearVelocity().y < 0)
			return State.FALLING;
		else if(Gdx.app.getType() == ApplicationType.Desktop && b2body.getLinearVelocity().x != 0 && Gdx.input.isTouched())
			return State.RUNNING_SHOOTING;
		else if(b2body.getLinearVelocity().x != 0)
			return State.RUNNING;
		else
			return State.STANDING;
	}
	
	/**
	 * Erstellt Box2D Body des Heros in der im Konstruktor uebergebenen Welt.
	 */
	public void defineHero() {
		BodyDef bdef = new BodyDef();
		bdef.position.set(150 / MyGdxGame.PPM, 100 / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);
		
		fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(25 / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData("hero");
		
		fixture = b2body.createFixture(fdef);
		fixture.setDensity(0f);
		b2body.resetMassData();
	}
	
	/**
	 * Hero schiesst mit der aktuell ausgeruesteten Munition auf den uebergebene Punkt.<br>
	 * @param x X-Koordinate des anvisierten Zielpunkts
	 * @param y Y-Koordinate des anvisierten Zielpunkts
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void shoot(float x, float y, float delta) {
		
		//Hero dreht sich in Schussrichtung
		if(x < b2body.getPosition().x && getState() == State.STANDING && runningRight){
			defaultTextureRegion.flip(true, false);
			runningRight = false;
		}
		else if(x > b2body.getPosition().x && getState() == State.STANDING && !runningRight){
			defaultTextureRegion.flip(true, false);
			runningRight = true;
		}
		
		//Nur alle 0.1 Sekunden schießen
		if((timePassed + delta) - lastShot >= 0.1){
			
			//Falls noch Lasermunition uebrig ist -> Laserschuss
			if(laserammo > 0){
				game.playLaserShot();
				laserbulletList.add(new Laserbullet(this, x, y));
				laserammo--;
			}
			
			//Falls keine Lasermunition, aber noch Triplemunition uebrig ist -> Tripleschuss
			else if(tripleammo > 0){
				game.playHeroShot();
				game.playHeroShot();
				game.playHeroShot();
				//Kugel #1 (oben)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y, 0.12);
					
					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}
					
					else {
						nextRemoved++;
					}
				} 
				
				else {
					bulletList.add(new Bullet(this, x, y, 9, 0.12));
				}
				
				
				//Kugel #2 (mitte)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y);
					
					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}
					
					else {
						nextRemoved++;
					}
				} 
				
				else {
					bulletList.add(new Bullet(this, x, y, 9));
				}
				
				
				//Kugel #3 (unten)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y, -0.12);
					
					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}
					
					else {
						nextRemoved++;
					}
				} 
				
				else {
					bulletList.add(new Bullet(this, x, y, 9, -0.12));
				}
				
				tripleammo--;
			}
			
			//Sonst Default-Waffe benutzen
			else{
				game.playHeroShot();
				if(bulletList.size() > bulletLimit){
					//Bestehende Bullet wiederverwenden
					bulletList.get(nextRemoved).setNew(x, y);
					
					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}
					
					else {
						nextRemoved++;
					}
				} 
				else {
					//Neue Bullet erzeugen
					bulletList.add(new Bullet(this, x, y, 9));
				}
			}				
			timePassed += delta;
			lastShot = timePassed;				
		}		
		else{
			timePassed += delta;
		}
	}
	
	/**
	 * Hero schiesst mit der aktuell ausgeruesteten Munition auf den uebergebene Punkt.<br>
	 * Multiplayer-Version: Sendet bei Schuss eine ShootingMessage an den Mitspieler um ihn darueber zu informieren.
	 * @param x X-Koordinate des anvisierten Zielpunkts
	 * @param y Y-Koordinate des anvisierten Zielpunkts
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 * @param connection Verbindung zum Mitspieler
	 */
	public void multiplayerShoot(float x, float y, float delta, Connection connection)
	{
		//Hero dreht sich in Schussrichtung
		if(x < b2body.getPosition().x && getState() == State.STANDING && runningRight){
			defaultTextureRegion.flip(true, false);
			runningRight = false;
		}
		else if(x > b2body.getPosition().x && getState() == State.STANDING && !runningRight){
			defaultTextureRegion.flip(true, false);
			runningRight = true;
		}

		//Nur alle 0.1 Sekunden schießen
		if((timePassed + delta) - lastShot >= 0.1){

			//Falls noch Lasermunition uebrig ist -> Laserschuss
			if(laserammo > 0){
				game.playLaserShot();
				laserbulletList.add(new Laserbullet(this, x, y));
				connection.sendTCP(new ShootingMessage(x,y));
				laserammo--;
			}

			//Falls keine Lasermunition, aber noch Triplemunition uebrig ist -> Tripleschuss
			else if(tripleammo > 0){
				game.playHeroShot();
				game.playHeroShot();
				game.playHeroShot();
				connection.sendTCP(new ShootingMessage(x,y));
				//Kugel #1 (oben)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y, 0.12);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}

				else {
					bulletList.add(new Bullet(this, x, y, 9, 0.12));
				}



				//Kugel #2 (mitte)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}

				else {
					bulletList.add(new Bullet(this, x, y, 9));
				}


				//Kugel #3 (unten)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y, -0.12);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}

				else {
					bulletList.add(new Bullet(this, x, y, 9, -0.12));
				}

				tripleammo--;
			}

			//Sonst Default-Waffe benutzen
			else{
				game.playHeroShot();
				connection.sendTCP(new ShootingMessage(x,y));
				if(bulletList.size() > bulletLimit){
					//Bestehende Bullet wiederverwenden
					bulletList.get(nextRemoved).setNew(x, y);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}
				else {
					//Neue Bullet erzeugen
					bulletList.add(new Bullet(this, x, y, 9));
				}
			}
			timePassed += delta;
			lastShot = timePassed;
		}
		else{
			timePassed += delta;
		}

	}
	
	/**
	 * Erzwingt Schuss von Hero (fuer Multiplayer benoetigt)
	 * @param x X-Koordinate des anvisierten Zielpunkts
	 * @param y Y-Koordinate des anvisierten Zielpunkts
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void forceShoot(float x, float y, float delta)
	{
		//Hero dreht sich in Schussrichtung
		if(x < b2body.getPosition().x && getState() == State.STANDING && runningRight){
			defaultTextureRegion.flip(true, false);
			runningRight = false;
		}
		else if(x > b2body.getPosition().x && getState() == State.STANDING && !runningRight){
			defaultTextureRegion.flip(true, false);
			runningRight = true;
		}
			//Falls noch Lasermunition uebrig ist -> Laserschuss
			if(laserammo > 0){
				game.playLaserShot();
				laserbulletList.add(new Laserbullet(this, x, y));
				laserammo--;
			}

			//Falls keine Lasermunition, aber noch Triplemunition uebrig ist -> Tripleschuss
			else if(tripleammo > 0){
				game.playHeroShot();
				game.playHeroShot();
				game.playHeroShot();
				//Kugel #1 (oben)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y, 0.12);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}

				else {
					bulletList.add(new Bullet(this, x, y, 9, 0.12));
				}


				//Kugel #2 (mitte)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}

				else {
					bulletList.add(new Bullet(this, x, y, 9));
				}


				//Kugel #3 (unten)
				if(bulletList.size() > bulletLimit){
					bulletList.get(nextRemoved).setNew(x, y, -0.12);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}

				else {
					bulletList.add(new Bullet(this, x, y, 9, -0.12));
				}

				tripleammo--;
			}

			//Sonst Default-Waffe benutzen
			else{
				game.playHeroShot();
				if(bulletList.size() > bulletLimit){
					//Bestehende Bullet wiederverwenden
					bulletList.get(nextRemoved).setNew(x, y);

					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}

					else {
						nextRemoved++;
					}
				}
				else {
					//Neue Bullet erzeugen
					bulletList.add(new Bullet(this, x, y, 9));
				}
			}
			timePassed += delta;
			lastShot = timePassed;


	}
	
	public World getWorld(){
		return world;
	}
	
	public ArrayList<Bullet> getBulletList(){
		return bulletList;
	}
	
	public ArrayList<Laserbullet> getLaserBulletList(){
		return laserbulletList;
	}
	
	public int getHealth(){
		return this.health;
	}
	
	public void setHealth(int health){
		this.health = health;
		
		if(this.health>100)
		{
			this.health = 100;
		}
		if(this.health<=0)
		{
			this.health = 0;
		}
		
	}
	
	public int getLives(){
		return this.lives;
	}
	
	public void setLives(int lives){
		this.lives = lives;

		if(this.lives<0)
		{
			this.lives = 0;
		}
		
	}	

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	public int getAmmo() {
		return ammo;
	}

	public void setAmmo(int ammo) {
		this.ammo = ammo;
	}

	public Body getBody(){
		return this.b2body;
	}
	
	public void standingOnPlatform(boolean b) {
		standingOnPlatform = b;
	}

	public void walkingOnPlatform(boolean b) {
		walkingOnPlatform = b;
	}

	public void setBulletList(ArrayList<Bullet> bulletList) {
		this.bulletList = bulletList;
	}
	
	public void setLaserBulletList(ArrayList<Laserbullet> laserbulletList) {
		this.laserbulletList = laserbulletList;
	}
	
	public void setMovingPlatform(MovingPlatform mp){
		this.movingPlatform = mp;
	}

	public float getInvulnerabilityTime() {
		return invulnerabilityTime;
	}

	public void setInvulnerabilityTime(float invulnerabilityTime) {
		this.invulnerabilityTime = invulnerabilityTime;
		
		if(this.invulnerabilityTime <= 0)
		{
			this.invulnerabilityTime = 0;
			//Zurueck zur Standard-Farbe wechseln
			this.setColor(Color.WHITE);
		}
	}
	
	public boolean isInvulnerable(){
		return this.getInvulnerabilityTime() > 0;
	}
	
	/**
	 * Aufgerufen, wenn der Hero Schaden erleidet/stirbt. <br>
	 * Macht den Hero fuer eine kurze Zeit unverwundbar und faerbt ihn rot.
	 * @param invulnerabilityTime Dauer fuer die Hero unverwundbar sein soll
	 */
	public void setDamageEffect(float invulnerabilityTime){
		game.playHurt();
		this.setInvulnerabilityTime(invulnerabilityTime);
		this.setColor(Color.RED);
	}

	public int getLaserAmmo() {
		return laserammo;
	}

	public void setLaserAmmo(int i) {
		laserammo = i;
	}

	public int getTripleAmmo() {
		return tripleammo;
	}

	public void setTripleAmmo(int i) {
		tripleammo = i;
	}
	
	/**
	 * Gibt die Anzahl an verbleibenden Schuessen fuer die aktuelle Waffe zurueck. 
	 * (Priorität: Laser>Triple) <br> Fuer die Munitionsanzeige verwendet.
	 * @return Anzahl an verbleibenden Schuessen fuer die aktuelle Waffe
	 */
	public int getCurrentAmmo(){
		if(this.getLaserAmmo()>0){
			return this.getLaserAmmo();
		}
		else if(this.getTripleAmmo()>0){
			return this.getTripleAmmo();
		}
		else{
			return 0;
		}
			
	}
	
	//Aufrufen wenn ein Level beendet wird
	public int getSavedScore(){
		return savedScore;
	}
	
	public void setSavedScore(int value){
		savedScore = value;
	}
	
	public void updateSavedScore(){
		savedScore = score;
	}
	
}
