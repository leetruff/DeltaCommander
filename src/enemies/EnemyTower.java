package enemies;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.GameScreen;
import com.mygdx.game.Hero;
import com.mygdx.game.MyGdxGame;

public class EnemyTower extends Sprite {

	//Wie viele Bullets duerfen gleichzeitig existieren
	private static final int bulletLimit = 12;
	
	Texture texture;
	Sprite sprite;
	Rectangle rectangle;
	
	MyGdxGame game;
	
	World world;
	public Body b2body;
	
	FixtureDef fdef;
	
	boolean lookingRight;
	
	static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/enemytower.png"));
	TextureRegion defaultTextureRegion;
	
	ArrayList<EnemySmallBullet> bulletList;
	
    float timePassed;
    float lastShot;
    int nextRemoved;
    int life;
    int points;
    GameScreen screen;
    
    //Hero objekt, damit wir einfach positionen auslesen koennen fuer AI
    Hero hero;
    
	
	public EnemyTower(World world, Hero hero, float xPos, float yPos, boolean flipY, MyGdxGame game){
		
		super(defaultTexture);
		
		this.game = game;
		this.world = world;
		defineEnemy(xPos, yPos);
		
		
		if(flipY){
			this.flip(false, true);
		}
		
		this.hero = hero;
		

		defaultTextureRegion = new TextureRegion(defaultTexture);
		
		setBounds(0, 0, 50 / MyGdxGame.PPM, 50 / MyGdxGame.PPM);
		this.setPosition(0, 0);
		
		bulletList = new ArrayList<EnemySmallBullet>();
		this.life = 100;
		this.points = 100;
		this.nextRemoved = 1;
		
	}
	

	//sprite updaten und x-flip falls noetig
	public void update(float delta){
		
		//Alle Projektile updaten
		for(int i = 0; i < bulletList.size(); i++){
			bulletList.get(i).update(delta);
		}
		
		setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2 + 10 / MyGdxGame.PPM);
		
		if(hero.getBody().getPosition().x > b2body.getPosition().x && !lookingRight){
			this.flip(true, false);
			lookingRight = true;
		}
		
		else if(hero.getBody().getPosition().x < b2body.getPosition().x && lookingRight){
			this.flip(true, false);
			lookingRight = false;
		}
		
		//Hero angreifen wenn in range
		//a^2 + b^2 = c^2 fuer Entfernung
		double xDif = Math.abs(b2body.getPosition().x - hero.b2body.getPosition().x);
		double yDif = Math.abs(b2body.getPosition().y - hero.b2body.getPosition().y);
		
		if(Math.sqrt(xDif * xDif + yDif * yDif) < 500 / MyGdxGame.PPM){
			shoot(hero.b2body.getPosition().x, hero.b2body.getPosition().y, delta);
		}
	}
	
	
	//erstellt box2d body 
	public void defineEnemy(float xPos, float yPos) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(xPos / MyGdxGame.PPM, yPos / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.StaticBody;
		b2body = world.createBody(bdef);
		
		b2body.setUserData(this);
		
		fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(20 / MyGdxGame.PPM, 10 / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData("enemytower");
	}

	public void shoot(float x, float y, float delta) {
		
		//Nur alle 0.3 Sekunden schießen
		if((timePassed + delta) - lastShot >= 0.3){
			game.playTurretShot();
			if(bulletList.size() > bulletLimit){
				bulletList.get(nextRemoved).setNew(x, y);;
				if(nextRemoved  >= bulletList.size() - 1){
					nextRemoved = 1;
				} else {
					nextRemoved++;
				}
			} else {
				bulletList.add(new EnemySmallBullet(this, x, y, 5));
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
	
	public ArrayList<EnemySmallBullet> getBulletList(){
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

	public void setBulletList(ArrayList<EnemySmallBullet> bulletList) {
		this.bulletList = bulletList;
	}
}


