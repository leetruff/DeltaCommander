package enemies;

import com.badlogic.gdx.Gdx;
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

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hermann on 27.06.16.
 */
public class Boss extends Sprite {

    static Texture defaultTexture = new Texture(Gdx.files.internal("boss/BossStanding.png"));
    World world;
    public Body body;

    FixtureDef fdef;

    Animation bossExplode;
    static TextureRegion jumping = new TextureRegion(new Texture(Gdx.files.internal("boss/BossJump.png")));
    TextureRegion region;
    TextureRegion shooting = new TextureRegion(new Texture(Gdx.files.internal("boss/BossShot.png")));

    MyGdxGame game;
    
    int life;
    float explosionTimer;
    TextureAtlas atlas;
    public boolean defeated;

    public enum State {FALLING, JUMPING, STANDING, SHOOTING};
    public State currentState, previousState;

    Hero hero;
    OrthographicCamera cam;

    boolean jumpright;
    boolean flipped;

    //Geschosse
    ArrayList<BossBullet> bulletList;
    private static final int bulletLimit = 12;
    float timePassed;
    float lastShot;
    int nextRemoved;
    boolean spawned;
    boolean online;

    //Aktionen
    //Array mit Aktionen, das immer wieder durchlaufen wird, 1 steht für jump, 2 für schießen
    int[] actions={1,2,2,1,1};
    int currentAction;
    //Timer, damit zwischen den Aktionen bisschen Zeit ist
    long actionTimerStart;
    long actionTimerEnd;
    //Timer für Schussgeschwindkeit
    long shootingTimerStart;
    long shootingTimerEnd;
    //Anzahl der Schüsse, zufällig gewürfelt
    int shootCounter;
    //Anzahl der Shakes, muss ungerade Zahl sein
    int shake;
    Random rand;

    /**
     * Boss Klasse für "Base"-Level
     * @param world Aktuelle Welt
     * @param hero Aktueller Hero
     * @param x X-Position des Erscheinens
     * @param y Y-Position des Erscheinens
     * @param cam GameCam fuer Shake-Effekt
     * @param game MyGdxGame fuer Sounds
     */
    public Boss(World world, Hero hero, float x, float y, OrthographicCamera cam, MyGdxGame game)
    {
        super(jumping);
        this.cam=cam;
        this.hero=hero;
        this.world=world;
        this.game=game;
        currentState=State.FALLING;
        region= new TextureRegion(defaultTexture);
        setBounds(2,2,250/MyGdxGame.PPM,200/MyGdxGame.PPM);
        defineBody(x,y);
        bulletList = new ArrayList<BossBullet>();
        life =3000;
        jumpright=false;
        flipped = false;
        currentAction=0;
        actionTimerStart=System.currentTimeMillis();
        spawned=false;
        defeated=false;
        rand = new Random();
        atlas = new TextureAtlas(Gdx.files.internal("boss/bossExplosion.pack"));
        Array<TextureRegion> explosionFrames= new Array<TextureRegion>();
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion01").getTexture(), 290, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion02").getTexture(), 386, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion03").getTexture(), 2, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion04").getTexture(), 194, 2, 94, 94));
        explosionFrames.add(new TextureRegion(atlas.findRegion("explosion05").getTexture(), 98, 2, 94, 94));

        bossExplode= new Animation(0.08f,explosionFrames);
        explosionTimer=0;
        online =false;

    }
    public void defineBody(float xpos,float ypos)
    {
        BodyDef bdef = new BodyDef();
        bdef.position.set(xpos, ypos);
        bdef.fixedRotation=true;
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        body.setUserData(this);

        fdef = new FixtureDef();
        fdef.density= 10.0f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(60 / MyGdxGame.PPM, 100 / MyGdxGame.PPM);


        fdef.shape = shape;
        body.createFixture(fdef).setUserData("boss");
        shake =0;

    }

    /**
     * Methode mit der der Boss über den Bildschirmrand springt, wie weit nach rechts bzw. links ist zufällig
     */
    public void jump()
    {
            int power = rand.nextInt(3) + 16;
            if (jumpright) {
                body.applyLinearImpulse((online)? 17:power, 100f, body.getPosition().x, body.getPosition().y, true);
                setRegion(jumping);
                jumpright = !jumpright;
            } else {
                body.applyLinearImpulse((online)?-17:-power, 100f, body.getPosition().x, body.getPosition().y, true);
                setRegion(jumping);
                jumpright = !jumpright;

            }
            currentState=State.JUMPING;
    }
    public void update(){
        if(!(life <=0)) {
            if (shake != 0) {
            	if(shake == 31){
            		game.playBossTremble();
            	}
            	shakeScreen();      
            }
            if (currentState == State.STANDING) {
                actionTimerEnd = System.currentTimeMillis();
                if (actionTimerEnd - actionTimerStart > 1500) {
                    if (currentAction == actions.length) {
                        currentAction = 0;
                    }
                    //Steht der Boss wird eine der Aktionen ausgeführt, 1 steht dabei für einen Sprung, 2 für schießen
                    if (hero.getLives() != 0) {
                        switch (actions[currentAction]) {

                            case 1:
                                jump();
                                currentAction++;
                                break;
                            case 2:
                                currentState = State.SHOOTING;
                                shootCounter =(online)?5: rand.nextInt(7) + 2;
                                shootingTimerStart = System.currentTimeMillis();
                                currentAction++;
                                setRegion(shooting);
                                break;
                        }
                    }

                }

            }
            if (currentState == State.SHOOTING) {
                if (shootCounter == 0) {
                    actionTimerStart = System.currentTimeMillis();
                    setRegion(region);
                    currentState = State.STANDING;
                }
                shootingTimerEnd = System.currentTimeMillis();
                if (shootingTimerEnd - shootingTimerStart > 500) {
                    shootHero();
                    shootCounter--;
                    shootingTimerStart = System.currentTimeMillis();
                }
            }
            //Alle Projektile updaten
            for (int i = 0; i < bulletList.size(); i++) {
                bulletList.get(i).update(Gdx.graphics.getDeltaTime());
            }
            setPosition((!flipped) ? body.getPosition().x - getWidth() / 1.7f : body.getPosition().x -110/MyGdxGame.PPM, body.getPosition().y - getHeight() / 2);
            if (currentState == State.JUMPING) {
                if (body.getLinearVelocity().y < 0) {
                    flipped = !flipped;
                    shooting.flip(true, false);
                    jumping.flip(true, false);
                    setRegion(jumping);
                    currentState = State.FALLING;
                }


            }
            if (currentState == State.FALLING) {
                if (body.getLinearVelocity().y == 0) {

                    if (spawned) {
                        shake = 31;
                        actionTimerStart = System.currentTimeMillis();
                        region.flip(true, false);
                        setRegion(region);
                        currentState = State.STANDING;

                    } else {
                        actionTimerStart = System.currentTimeMillis();
                        shake = 31;
                        currentState = State.STANDING;
                        setRegion(region);
                        spawned = true;
                    }
                }
            }
        }
        else
        {
        	if(explosionTimer == 0){
            	game.playBossExplode();
            	hero.setScore(hero.getScore()+1000);
        	}
            explode();
        }
    }
    public void getState()
    {
        if(body.getLinearVelocity().y>0)
        {
            currentState=State.JUMPING;
        }
        if(body.getLinearVelocity().y>0)
        {
            currentState=State.JUMPING;
        }
    }
    public void shootHero()
    {
        shoot(hero.b2body.getPosition().x, hero.b2body.getPosition().y, Gdx.graphics.getDeltaTime());
    }
    public void shoot(float x, float y, float delta) {
    		game.playBossShot();
            if(bulletList.size() > bulletLimit){
                bulletList.get(nextRemoved).setNew(x, y);;
                if(nextRemoved  >= bulletList.size() - 1){
                    nextRemoved = 1;
                } else {
                    nextRemoved++;
                }
            } else {
                bulletList.add(new BossBullet(this, x, y, 9,flipped));
            }
            timePassed += delta;
            lastShot = timePassed;
    }

    public Body getBody() {
        return body;
    }

    public World getWorld() {
        return world;
    }

    public ArrayList<BossBullet> getBulletList() {
        return bulletList;
    }
    //Methode um den Bildschirm zu shaken wenn der Boss landet
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
            defeated=true;
        }
    }

    public void setBulletList(ArrayList<BossBullet> bulletList) {
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
        for (BossBullet b:bulletList)
        {
            world.destroyBody(b.getBody());
            b.getBody().setUserData(null);
            b.setBody(null);
        }
    }

    /**
     * falls online gespielt wird, darf keinen Zufall geben.
     */
    public void switchToOnline()
    {
        online =true;
    }
}
