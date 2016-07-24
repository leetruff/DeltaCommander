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
public class TankBoss extends Sprite {

    static Texture defaultTexture = new Texture(Gdx.files.internal("boss/TankBoss.png"));
    World world;
    public Body body;

    FixtureDef fdef;

    Animation bossExplode;
    TextureRegion region;
    MyGdxGame game;
    int life;
    float explosionTimer;
    TextureAtlas atlas;
    public boolean defeated;

    public enum State {DRIVING, STANDING, SHOOTING,SPRINTING};
    public State currentState;

    Hero hero;
    OrthographicCamera cam;

    boolean jumpright;
    boolean flipped;

    //Geschosse
    ArrayList<TankBossBullet> bulletList;
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
    //Ränder des Bossfights
    float x1,x2;

    /**
     * Tank-Boss Klasse, Tank Boss muss rechts vom aktuellen Bildschirm gespawnt werden, fährt dann selbst in den Kampfbereich.
     * Bounds sind dafür da, damit der Boss sich während des Kampfes nicht zu weit fährt.
     * @param world Aktuelle Welt
     * @param hero Aktueller Hero
     * @param x X-Position des Erscheinens
     * @param y Y-Position des Erscheinens
     * @param cam GameCam fuer Shake-Effekt
     * @param game MyGdxGame fuer Sounds
     * @param boundx1 Rechte Grenze des Bossfights(X-Koordinate)
     * @param boundx2 Linke Grenze des Bossfights (Y-Koordinate)
     */
    public TankBoss(World world, Hero hero, float x, float y, OrthographicCamera cam, MyGdxGame game,float boundx1, float boundx2)
    {
        super(defaultTexture);
        this.cam=cam;
        this.hero=hero;
        this.world=world;
        this.game=game;
        currentState= State.DRIVING;
        region= new TextureRegion(defaultTexture);
        setRegion(region);
        setBounds(2,2,200/MyGdxGame.PPM,100/MyGdxGame.PPM);
        defineBody(x,y);
        bulletList = new ArrayList<TankBossBullet>();
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
        currentState=State.DRIVING;
        bossExplode= new Animation(0.08f,explosionFrames);
        explosionTimer=0;
        online =false;
        setBattleBounds(boundx1,boundx2);

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
        shape.setAsBox(90 / MyGdxGame.PPM, 37/ MyGdxGame.PPM);


        fdef.shape = shape;
        body.createFixture(fdef).setUserData("tankboss");
        shake =0;

    }

    public void update(){
        if(!(life <=0)) {

            if (shake != 0) {
            	if(shake == 31){
            		game.playBossTremble();
            	}
            	shakeScreen();      
            }
            if(currentState==State.DRIVING)
            {
                if(!spawned) {
                    body.applyLinearImpulse(-15, 0, body.getPosition().x, body.getPosition().y, true);
                    spawned=true;
                }
                if(body.getLinearVelocity().x==0) {
                    currentState = State.STANDING;
                    actionTimerStart = System.currentTimeMillis();

                }
            }
            if (currentState == State.STANDING) {
                actionTimerEnd = System.currentTimeMillis();
                if (actionTimerEnd - actionTimerStart > 1000) {
                    if (currentAction == actions.length) {
                        currentAction = 0;
                    }
                    //Steht der Boss wird eine der Aktionen ausgeführt, 1 steht dabei für einen Sprung, 2 für schießen
                    if (hero.getLives() != 0) {
                        switch (actions[currentAction]) {

                            case 1:
                                sprint();
                                currentAction++;
                                break;
                            case 2:
                                currentState = State.SHOOTING;
                                shootCounter =(online)?5: rand.nextInt(7) + 2;
                                shootingTimerStart = System.currentTimeMillis();
                                currentAction++;
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
            if(currentState==State.SPRINTING)
            {
                if((body.getPosition().x<x1&&flipped)||(body.getPosition().x>x2&&!flipped)||body.getLinearVelocity().x==0)
                {
                    body.setLinearVelocity(0,0);
                    region.flip(true,false);
                    setRegion(region);
                    currentState=State.STANDING;
                    shake=0;
                    actionTimerStart = System.currentTimeMillis();
                }
            }
            //Alle Projektile updaten
            for (int i = 0; i < bulletList.size(); i++) {
                bulletList.get(i).update(Gdx.graphics.getDeltaTime());
            }
            setPosition((!flipped) ? body.getPosition().x - getWidth() / 1.7f : body.getPosition().x -110/MyGdxGame.PPM, body.getPosition().y - getHeight() / 2);

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
                bulletList.add(new TankBossBullet(this, x, y, 9,flipped));
            }
            timePassed += delta;
            lastShot = timePassed;
    }

    /**
     * Methode die den Panzer schnell nach rechts bzw nach links fahren lässt.
     */
    public void sprint()
    {
        if(flipped)
        {
            body.applyLinearImpulse(40,0,body.getPosition().x,body.getPosition().y,true);
            flipped=false;


        }
        else
        {
            body.applyLinearImpulse(-40,0,body.getPosition().x,body.getPosition().y,true);
            flipped=true;
        }
        currentState=State.SPRINTING;
        shake=31;
    }

    public Body getBody() {
        return body;
    }

    public World getWorld() {
        return world;
    }

    public ArrayList<TankBossBullet> getBulletList() {
        return bulletList;
    }
    //Methode um den Bildschirm zu shaken wenn der Boss-Gegner fährt
    public void shakeScreen()
    {
        if(shake!=1) {
            cam.position.set(cam.position.x, (shake%2==1)?cam.position.y+0.03f:cam.position.y-0.03f, 0);
        }
        shake=(shake%2==1)? 20:21;
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

    public void setBulletList(ArrayList<TankBossBullet> bulletList) {
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
        for (TankBossBullet b:bulletList)
        {
            world.destroyBody(b.getBody());
            b.getBody().setUserData(null);
            b.setBody(null);
        }
    }
    public void setBattleBounds(float x1, float x2)
    {
        this.x1=x1;
        this.x2=x2;

    }

    /**
     * Falls online, darf es keinen zufall geben
     */
    public void switchToOnline()
    {
        online =true;
    }
}
