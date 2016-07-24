package enemies;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameScreen;
import com.mygdx.game.Hero;
import com.mygdx.game.MyGdxGame;

public class EnemySoldier extends Sprite {

	//Wie viele Bullets duerfen gleichzeitig existieren
	private static final int bulletLimit = 12;
	
	Texture texture;
	Sprite sprite;
	Rectangle rectangle;
	
	MyGdxGame game;
	
	World world;
	public Body b2body;
	
	FixtureDef fdef;
	
	public enum State {FALLING, JUMPING, STANDING, RUNNING};
	public State currentState, previousState;
	
	float stateTimer;
	boolean runningRight;
	TextureAtlas atlas;
	
	Animation enemyRun;
	static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/enemysoldierstand.png"));
	TextureRegion defaultTextureRegion;
	TextureRegion jumpTextureRegion;
	
	ArrayList<EnemyBullet> bulletList;
	
    float timePassed;
    float lastShot;
    int nextRemoved;
    int life;
    int points;
    GameScreen screen;
    
    //Hero objekt, damit wir einfach positionen auslesen koennen fuer AI
    Hero hero;
    
	
	public EnemySoldier(World world, Hero hero, float xPos, float yPos, MyGdxGame game){
		
		super(defaultTexture);
		
		this.game = game;
		this.world = world;
		defineEnemy(xPos, yPos);
		
		currentState = State.STANDING;
		previousState = State.STANDING;
		stateTimer = 0;
		runningRight = false;
		this.hero = hero;
		
		atlas = new TextureAtlas(Gdx.files.internal("sprites/enemysoldierrunanimation.pack"));

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
		
		
		enemyRun = new Animation(0.08f, frames);
		frames.clear();

		//Textureregion Jump
		jumpTextureRegion = new TextureRegion(new Texture(Gdx.files.internal("sprites/enemysoldierjump.png")));
		defaultTextureRegion = new TextureRegion(defaultTexture);
		
		setBounds(0, 0, 50 / MyGdxGame.PPM, 50 / MyGdxGame.PPM);
		this.setPosition(0, 0);
		
		bulletList = new ArrayList<EnemyBullet>();
		this.life = 100;
		this.points = 100;
		this.nextRemoved = 1;
	}
	
	public EnemySoldier(World world, Hero hero, GameScreen screen, int xPos, int yPos, MyGdxGame game) {
		this(world, hero, xPos, yPos, game);
		this.screen = screen;	
	}

	//sprite updaten und x-flip falls noetig
	public void update(float delta){
		
		//Alle Projektile updaten
		for(int i = 0; i < bulletList.size(); i++){
			bulletList.get(i).update(delta);
		}
		
		setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
		setRegion(getFrame(delta));
		
		if(hero.getBody().getPosition().x > b2body.getPosition().x && !runningRight){
			this.flip(true, false);
			runningRight = true;
		}
		
		else if(hero.getBody().getPosition().x < b2body.getPosition().x && runningRight){
			this.flip(true, false);
			runningRight = false;
		}
		
		//Hero angreifen wenn in range
		//a^2 + b^2 = c^2 fuer Entfernung
		double xDif = Math.abs(b2body.getPosition().x - hero.b2body.getPosition().x);
		double yDif = Math.abs(b2body.getPosition().y - hero.b2body.getPosition().y);
		
		if(Math.sqrt(xDif * xDif + yDif * yDif) < 500 / MyGdxGame.PPM){
			shoot(hero.b2body.getPosition().x, hero.b2body.getPosition().y, delta);
		}
	}
	
	public TextureRegion getFrame(float delta){
		currentState = getState();
		
		TextureRegion region;
		
		switch(currentState){
		case JUMPING:
			region = jumpTextureRegion; break;
		case RUNNING:
			region = enemyRun.getKeyFrame(stateTimer, true); break;
		case FALLING:
		case STANDING:
		default:
			region = defaultTextureRegion; break;
		}
		
		if((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()){
				region.flip(true, false);
				runningRight = false;
		}
		
		else if((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()){
				region.flip(true, false);
				runningRight = true;
		}
		
		stateTimer = currentState == previousState ? stateTimer + delta : 0;
		previousState = currentState;
		return region;
	}
	
	//brauchen wir spaeter fuer verschiedene Animationen
	public State getState(){
		if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING))
			return State.JUMPING;
		else if(b2body.getLinearVelocity().y < 0)
			return State.FALLING;
		else if(b2body.getLinearVelocity().x != 0)
			return State.RUNNING;
		else
			return State.STANDING;
	}
	
	//erstellt box2d body 
	public void defineEnemy(float xPos, float yPos) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(xPos / MyGdxGame.PPM, yPos / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);
		
		b2body.setUserData(this);
		
		fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(25 / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData("enemysoldier");
	}

	public void shoot(float x, float y, float delta) {
		
		//Nur alle 0.3 Sekunden schießen
		if((timePassed + delta) - lastShot >= 0.3){
			game.playEnemySoldierShot();
			if(bulletList.size() > bulletLimit){
				bulletList.get(nextRemoved).setNew(x, y);;
				if(nextRemoved  >= bulletList.size() - 1){
					nextRemoved = 1;
				} else {
					nextRemoved++;
				}
			} else {
				bulletList.add(new EnemyBullet(this, x, y, 9));
			}
			timePassed += delta;
			lastShot = timePassed;
		}
		
		else{
			timePassed += delta;
		}
	}
	
	public World getWorld(){
		return world;
	}
	
	public ArrayList<EnemyBullet> getBulletList(){
		return bulletList;
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

	public Body getBody(){
		return this.b2body;
	}

	public void setBulletList(ArrayList<EnemyBullet> bulletList) {
		this.bulletList = bulletList;
	}
}

