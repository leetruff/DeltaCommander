package enemies;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameScreen;
import com.mygdx.game.Hero;
import com.mygdx.game.MyGdxGame;

public class EnemyHoming extends Sprite {
	
	//Wie viel Schaden macht die Drohne?
	private static final int damage = 5;
	
	Texture texture;
	Sprite sprite;
	Rectangle rectangle;
	
	MyGdxGame game;
	
	World world;
	public Body b2body;
	
	FixtureDef fdef;
	
	public enum State {HOMING, IDLE};
	public State currentState, previousState;
	float stateTimer;
	
	Animation enemyFly;
	static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/droneidle.png"));
	TextureRegion defaultTextureRegion;
	
	boolean isHoming;
    long lastDmg;
    int life;
    int points;
    int homingRadius;
    GameScreen screen;
    
    //Hero objekt, damit wir einfach positionen auslesen koennen fuer AI
    Hero hero;
    
    public EnemyHoming(World world, Hero hero, float xPos, float yPos, MyGdxGame game, int homingRadius){
    	this(world, hero, xPos, yPos, game);
    	this.homingRadius = homingRadius;
    }
    
	public EnemyHoming(World world, Hero hero, GameScreen screen, int xPos, int yPos, MyGdxGame game, int homingRadius) {
		this(world, hero, screen, xPos, yPos, game);
		this.homingRadius = homingRadius;
	}
    
	public EnemyHoming(World world, Hero hero, float xPos, float yPos, MyGdxGame game){
		
		super(defaultTexture);
		
		this.game = game;
		this.world = world;
		defineEnemy(xPos, yPos);
		
		isHoming = false;
		currentState = State.IDLE;
		previousState = State.IDLE;
		stateTimer = 0;
		this.hero = hero;
		lastDmg = 0;
		homingRadius = 500;
		
		//Fly Animation Frames
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(new Texture(Gdx.files.internal("sprites/droneoff.png"))));
		frames.add(new TextureRegion(new Texture(Gdx.files.internal("sprites/droneondmg1.png"))));
		frames.add(new TextureRegion(new Texture(Gdx.files.internal("sprites/droneoffdmg2.png"))));
		frames.add(new TextureRegion(new Texture(Gdx.files.internal("sprites/droneondmg3.png"))));
		
		
		enemyFly = new Animation(0.3f, frames);
		frames.clear();

		defaultTextureRegion = new TextureRegion(defaultTexture);
		
		setBounds(0, 0, 50 / MyGdxGame.PPM, 50 / MyGdxGame.PPM);
		this.setPosition(0, 0);
		
		this.life = 50;
		this.points = 100;
	}
	
	public EnemyHoming(World world, Hero hero, GameScreen screen, int xPos, int yPos, MyGdxGame game) {
		this(world, hero, xPos, yPos, game);
		this.screen = screen;	
	}

	//sprite updaten und x-flip falls noetig
	public void update(float delta){
		
		setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
		setRegion(getFrame(delta));
		
		//Hero angreifen wenn in range
		//a^2 + b^2 = c^2 fuer Entfernung
		double xDif = Math.abs(b2body.getPosition().x - hero.b2body.getPosition().x);
		double yDif = Math.abs(b2body.getPosition().y - hero.b2body.getPosition().y);
		
		if(!isHoming && Math.sqrt(xDif * xDif + yDif * yDif) < homingRadius / MyGdxGame.PPM){
			isHoming = true;
			game.playHoming();
		}
		if(isHoming){
			if(Math.sqrt(xDif * xDif + yDif * yDif) < 50 / MyGdxGame.PPM && System.currentTimeMillis() - lastDmg >= 300){
				//Nur alle 0.3 Sekunden Schaden zufuegen
				hero.setHealth(hero.getHealth() - damage);
				lastDmg = System.currentTimeMillis();
			}
			getBody().applyLinearImpulse(new Vector2(hero.getX() - getX(), hero.getY() - getY()).limit(0.05f), getBody().getWorldCenter(), true);
		}
	}
	
	public TextureRegion getFrame(float delta){
		currentState = getState();
		TextureRegion region;
		
		if(isHoming){
			region = enemyFly.getKeyFrame(stateTimer, true);
		} else {
			region = defaultTextureRegion;
		}
		
		stateTimer = currentState == previousState ? stateTimer + delta : 0;
		previousState = currentState;
		return region;
	}
	
	public State getState(){
		if(isHoming){
			return State.HOMING;
		}else{ 
			return State.IDLE;
		}
	}
	
	//erstellt box2d body 
	public void defineEnemy(float xPos, float yPos) {
		BodyDef bdef = new BodyDef();
		bdef.gravityScale = 0;
		bdef.position.set(xPos / MyGdxGame.PPM, yPos / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);
		b2body.getMassData().mass = 0.1f;
		b2body.setUserData(this);
		
		fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(11 / MyGdxGame.PPM);
		
		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData("enemyhoming");
	}

	public void onHit() {
		//Nur alle 0.3 Sekunden Schaden zufuegen
		if(System.currentTimeMillis() - lastDmg >= 300){
			//Schaden zufuegen
			hero.setHealth(hero.getHealth() - damage);
			lastDmg = System.currentTimeMillis();
		}
	}
	
	public World getWorld(){
		return world;
	}
	
	public int getLife(){
		return this.life;
	}
	
	public int getPoints(){
		return this.points;
	}
	
	public void setLife(int life){
		this.life = life;
		
		if(this.life>100)
		{
			this.life = 100;
		}
		if(this.life<0)
		{
			this.life = 0;
		}
	}
	
	public void damage(int damage){
		this.life = this.life - damage;
		if(this.life < 0){
			this.life = 0;
		}
	}

	public Body getBody(){
		return this.b2body;
	}
}

