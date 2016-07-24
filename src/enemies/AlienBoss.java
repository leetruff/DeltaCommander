package enemies;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Hero;
import com.mygdx.game.MyGdxGame;

/**
 * Created by Lars on 07.07.16.
 */
public class AlienBoss extends Sprite {
	
	static Texture defaultTexture = new Texture(Gdx.files.internal("boss/alienboss/boss1.png"));
	static TextureRegion defaultRegion = new TextureRegion(new Texture(Gdx.files.internal("boss/alienboss/boss1.png")));
    World world;
    public Body body;

    FixtureDef fdef;

    Animation bossExplode;
    Animation bossDefault;
    
    
    MyGdxGame game;
    
    int life;
    float explosionTimer;
    TextureAtlas atlas;
    public boolean defeated;
    
    public enum State {IDLING, SWIPING, SPAWNING_ADDS, FLYING_SHOOTING};
    public State currentState, previousState;
    float stateTimer;
    
    
    Hero hero;
    OrthographicCamera cam;
    
    ArrayList<AlienStacheln> stachelList;
    ArrayList<EnemySoldier> enemysoldierList;
    ArrayList<EnemyHoming> enemyhomingList;
    
    //Geschosse
    ArrayList<EnemyBullet> bulletList;
    private static final int bulletLimit = 20;
    float timePassed;
    float lastShot;
    int nextRemoved;
    
    
    //Aktionen
    //Array mit Aktionen, das immer wieder durchlaufen wird, 1 steht für jump, 2 für schießen
    
    
    //Timer für Schussgeschwindkeit
    long shootingTimerStart = -1;
    long shootingTimerEnd;
    
    //Anzahl der Schüsse, zufällig gewürfelt
    int shootCounter;
    
    //Anzahl der Shakes, muss ungerade Zahl sein
    int shake;
    Random rand;
    
    boolean spawned;
    boolean flyingright;
    private boolean swipingright;
	private boolean initialPositionReached = false;
	private boolean addsSpawned;
	private boolean firstrotation = true;
    
    
    public AlienBoss(World world, Hero hero, float x, float y, OrthographicCamera cam, MyGdxGame game)
    {
        super(defaultRegion);
        this.cam = cam;
        this.hero = hero;
        this.world = world;
        this.game = game;
        currentState = State.FLYING_SHOOTING;
        previousState = State.FLYING_SHOOTING;
        setBounds(0, 0, 128 / MyGdxGame.PPM, 128 / MyGdxGame.PPM);
        defineBody(x,y);
        bulletList = new ArrayList<EnemyBullet>();
        life = 5000;
        defeated = false;
        rand = new Random();
        atlas = new TextureAtlas(Gdx.files.internal("boss/bossExplosion.pack"));
        Array<TextureRegion> explosionFrames= new Array<TextureRegion>();
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion01").getTexture(), 290, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion02").getTexture(), 386, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion03").getTexture(), 2, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion04").getTexture(), 194, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion05").getTexture(), 98, 2, 94, 94));

        bossExplode = new Animation(0.08f,explosionFrames);
        explosionTimer = 0;
        
        
        atlas = new TextureAtlas(Gdx.files.internal("boss/alienboss/alienboss.pack"));
        Array<TextureRegion> defaultFrames = new Array<TextureRegion>();
        defaultFrames.add(new TextureRegion(atlas.findRegion("boss1").getTexture(), 1, 1, 64, 64));
        defaultFrames.add(new TextureRegion(atlas.findRegion("boss2").getTexture(), 67, 1, 64, 64));
        defaultFrames.add(new TextureRegion(atlas.findRegion("boss3").getTexture(), 133, 1, 64, 64));
        defaultFrames.add(new TextureRegion(atlas.findRegion("boss4").getTexture(), 199, 1, 64, 64));
        
        bossDefault = new Animation(0.2f, defaultFrames);
        
        this.setRegion(defaultRegion);
        
        flyingright = true;
        swipingright = true;
        
        stachelList = new ArrayList<AlienStacheln>();
        enemysoldierList = new ArrayList<EnemySoldier>();
        enemyhomingList = new ArrayList<EnemyHoming>();
    }
    
    public void defineBody(float xpos,float ypos)
    {
        BodyDef bdef = new BodyDef();
        bdef.position.set(xpos, ypos);
        bdef.fixedRotation = true;
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        body.setUserData(this);
        body.setGravityScale(0f);

        fdef = new FixtureDef();
        fdef.density= 10.0f;
        fdef.isSensor = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(52 / MyGdxGame.PPM, 52 / MyGdxGame.PPM);


        fdef.shape = shape;
        body.createFixture(fdef).setUserData("alienboss");
        shake = 0;
        
        this.setOrigin(this.getWidth() / 2, this.getHeight() / 2);
        System.out.println(xpos);
    }
    
    public void jump()
    {
    	
    }
    
    public void update(float delta){
		setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
		setRotation((float) (body.getAngle() * (180/Math.PI)));
		setRegion(getFrame(delta));
		
		
		if(shake > 0){
			shakeScreen();
		}
		
		if(spawned){
			
			if(currentState == State.IDLING){
				body.setLinearVelocity(0f, 0f);
				body.setTransform(29.229448f, 6f, 0f);
				
				setColor(Color.WHITE);
				
				if(getColor().a < 0.95f){
					fadeIn();
					
					if(getColor().a + 0.015f >= 0.95f){
						body.setActive(true);
					}
				}
				
				else{
					if(previousState == State.FLYING_SHOOTING){
						currentState = State.SWIPING;
						game.playDespawnAlien();
					}
					
					if(previousState == State.SWIPING){
						currentState = State.SPAWNING_ADDS;
						game.playDespawnAlien();
					}
					
					if(previousState == State.SPAWNING_ADDS){
						currentState = State.FLYING_SHOOTING;
						game.playDespawnAlien();
					}
				}
			}
			
			else if(currentState == State.FLYING_SHOOTING){
				if(shootingTimerStart == -1)
				shootingTimerStart = System.currentTimeMillis();
				
				body.setActive(true);
				patrol();
				shootCirle(delta);
				
				if(life < 4000 && firstrotation){
					currentState = State.IDLING;
					body.setActive(false);
					setAlpha(0f);
					game.playSpawnAlien();
					previousState = State.FLYING_SHOOTING;
				}
				
				if(life < 1000 && !firstrotation){
					currentState = State.IDLING;
					body.setActive(false);
					setAlpha(0f);
					game.playSpawnAlien();
					previousState = State.FLYING_SHOOTING;
				}
			}
			
			
			else if(currentState == State.SWIPING){
				
				
				if(!initialPositionReached){
					setInitialPosition();
				}
				
				else{
					body.setActive(true);
					setColor(Color.RED);
					swipe();
					
					if(life < 2500 && firstrotation){
						currentState = State.IDLING;
						body.setActive(false);
						setAlpha(0f);
						previousState = State.SWIPING;
						game.playSpawnAlien();
						destroySpikes();
						initialPositionReached = false;
					}
					
					if(life <= 0 && !firstrotation){
						setColor(Color.WHITE);
						destroySpikes();
						if(explosionTimer == 0){
							hero.setScore(hero.getScore()+2000);
							game.playBossExplode();
						}
						explode();
					}
				}
			}
			
			else if(currentState == State.SPAWNING_ADDS){
				body.setActive(false);
				
				if(getColor().a > 0 && !addsSpawned)
				fadeOut();
				
				else if(enemysoldierList.size() == 0 && enemyhomingList.size() == 0 && !addsSpawned){
					game.playSpawnAdds();
					enemysoldierList.add(new EnemySoldier(world, hero, 27.497463f * MyGdxGame.PPM, 4.864999f * MyGdxGame.PPM, game));
					enemysoldierList.add(new EnemySoldier(world, hero, 30.95172f * MyGdxGame.PPM, 4.864999f * MyGdxGame.PPM, game));
					
					enemyhomingList.add(new EnemyHoming(world, hero, 25.798496f * MyGdxGame.PPM, 4.364999f * MyGdxGame.PPM, game));
					enemyhomingList.add(new EnemyHoming(world, hero, 32.647842f * MyGdxGame.PPM, 4.364999f * MyGdxGame.PPM, game));
					System.out.println("Enemys Spawned");
					addsSpawned = true;
				}
				
				if(enemysoldierList.size() == 0 && enemyhomingList.size() == 0 && addsSpawned && !body.isActive() && getColor().a == 0){
					game.playSpawnAlien();
				}
				if(enemysoldierList.size() == 0 && enemyhomingList.size() == 0 && addsSpawned && getColor().a < 0.95f){
					body.setActive(true);
					fadeIn();
					
					if(getColor().a + 0.015f >= 0.95f){
						currentState = State.IDLING;
						body.setActive(false);
						setAlpha(0f);
						previousState = State.SPAWNING_ADDS;
						firstrotation = false;
					}
				}
				
			}
			
			
			
		}
		
    }
    
    
    private void destroySpikes() {
    	for(int i = 0; i < stachelList.size(); i++){
    		world.destroyBody(stachelList.get(i).body);
    	}
    	
    	for(int i = stachelList.size() - 1; i >= 0; i--){
    		stachelList.remove(i);
    	}
	}

	private void fadeIn(){
    	setAlpha(getColor().a + 0.015f);
    }
    
    private void fadeOut(){
    	setAlpha(getColor().a - 0.005f);
    }
    
	private void setInitialPosition() {
		
		if(getColor().a != 0 && body.getPosition().x != 25.3f && body.getPosition().y != 3.9f){
			fadeOut();
		}
		
		else if(body.getPosition().x != 25.3f && body.getPosition().y != 3.9f){
			body.setTransform(25.3f, 3.9f, -0.5f);
			game.playSpawnAlien();
		}
		
		else if(getColor().a + 0.015 <= 1 && body.getPosition().x == 25.3f && body.getPosition().y == 3.9f){
			fadeIn();
		}
		
		else{
			initialPositionReached = true;
			
			//Stacheln auf Platformen spawnen
			stachelList.add(new AlienStacheln(25.798496f + 0.02f, 4.864999f - 0.03f, world));
			stachelList.add(new AlienStacheln(27.497463f + 0.02f, 4.864999f - 0.03f, world));
			stachelList.add(new AlienStacheln(29.236805f, 4.864999f - 0.03f, world));
			stachelList.add(new AlienStacheln(30.95172f - 0.01f, 4.864999f - 0.03f, world));
			stachelList.add(new AlienStacheln(32.647842f, 4.864999f - 0.03f, world));
			
			System.out.println("stacheln gespawned");
		}
	}
	
	public ArrayList<AlienStacheln> getStachelList(){
		return stachelList;
	}
	
	public ArrayList<EnemySoldier> getSoldierList(){
		return enemysoldierList;
	}
	
	public ArrayList<EnemyHoming> getHomingList(){
		return enemyhomingList;
	}

	private void shootCirle(float delta) {
		
		//Schussfrequenz
		if((timePassed + delta) - lastShot >= 2.5){
			
			game.playBossShot();
			game.playBossShot();
			game.playBossShot();
			for(int i = 0; i <= 20; i++){
				
				if(bulletList.size() <= bulletLimit)
				bulletList.add(new EnemyBullet(body.getPosition().x, body.getPosition().y, (float) Math.toRadians(((360.0 / 20.0) * i)), 14, world));
				
				else {
					bulletList.get(nextRemoved).setNew(body.getPosition().x, body.getPosition().y, (float) Math.toRadians(((360.0 / 20.0) * i)));
					
					if(nextRemoved  >= bulletList.size() - 1){
						nextRemoved = 1;
					}
					
					else {
						nextRemoved++;
					}
				}
			}
			
			
			timePassed += delta;
			lastShot = timePassed;
		}
		
		else{
			timePassed += delta;
		}
	}

	private void patrol() {
		
		if(body.getPosition().x >= 33f)
			flyingright = false;
		
		else if(body.getPosition().x <= 25.3f)
			flyingright = true;
		
		
		if(flyingright){
			body.setLinearVelocity(1.5f, 0f);
		}
		
		else if(!flyingright){
			body.setLinearVelocity(-1.5f, 0f);
		}
	}
	
	private void swipe() {
		
		if(body.getPosition().x >= 33f){
			swipingright = false;
			
			if(body.getAngle() != 0.5f){
				body.setTransform(body.getPosition(), -body.getAngle());
				game.playBossTremble();
				shake = 21;
			}
		}
		
		else if(body.getPosition().x <= 25.3f){
			swipingright = true;
			
			if(body.getAngle() != -0.5f){
				body.setTransform(body.getPosition(), -body.getAngle());
				game.playBossTremble();
				shake = 21;
			}
		}
		
		if(swipingright){
			body.setLinearVelocity(3.5f, 0f);
		}
		
		else if(!swipingright){
			body.setLinearVelocity(-3.5f, 0f);
		}
	}

	public TextureRegion getFrame(float delta){
		
		TextureRegion region;
		region = bossDefault.getKeyFrame(stateTimer, true);
		
		stateTimer += delta;
		return region;
	}
    
    public void shootHero()
    {
        shoot(hero.b2body.getPosition().x, hero.b2body.getPosition().y, Gdx.graphics.getDeltaTime());
    }
    
    public void shoot(float x, float y, float delta) {
    	
    }

    public Body getBody() {
        return body;
    }

    public World getWorld() {
        return world;
    }
    
    public ArrayList<EnemyBullet> getBulletList() {
        return bulletList;
    }
    
    public void shakeScreen()
    {
        if(shake!=1) {
            cam.position.set(cam.position.x, (shake%2==1)?cam.position.y+0.1f:cam.position.y-0.1f, 0);
        }
        shake--;

    }
    
    public void explode()
    {
        if(!bossExplode.isAnimationFinished(explosionTimer))
        {
            setRegion(bossExplode.getKeyFrame(explosionTimer, false));
            explosionTimer += Gdx.graphics.getDeltaTime();
        }
        else
        {
            defeated = true;
        }
    }
    
    public void setBulletList(ArrayList<EnemyBullet> bulletList) {
        this.bulletList = bulletList;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getLife() {
        return life;
    }
    
    public void destroyBoss()
    {
        world.destroyBody(body);
        body.setUserData(null);
    }

	public void setSpawned(boolean b) {
		spawned = b;
	}
    
	public void setShake(int i){
		shake = i;
	}
}
