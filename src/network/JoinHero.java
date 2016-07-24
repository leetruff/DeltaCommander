package network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.mygdx.game.Bullet;
import com.mygdx.game.MyGdxGame;

import java.util.ArrayList;

/**
 * Created by hermann on 18.06.16.
 */
public class JoinHero extends Sprite {

    private static final int bulletLimit = 30;

    MyGdxGame game;
    World world;
    public Body b2body;
    FixtureDef fdef;
    static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/joinHero.png"));
    RopeJointDef ropeJoint;

    ArrayList<JoinHeroBullet> bulletList;

    float timePassed;
    float lastShot;
    int nextRemoved;

    boolean right;

    /**
     * Klasse für den JoinPlayer im Multiplayer-Modus
     * @param game
     * @param world
     */
    public JoinHero(MyGdxGame game, World world)
    {
        super(defaultTexture);
        setBounds(0, 0, 50 / MyGdxGame.PPM, 35 / MyGdxGame.PPM);
        this.game= game;
        this.world = world;
        bulletList = new ArrayList<JoinHeroBullet>();
        right=true;
        defineBody();
    }
    public void shoot(float x, float y, float delta)
    {

        //Nur alle 0.1 Sekunden schießen
        if((timePassed + delta) - lastShot >= 0.1){
            game.playShoot();
            if(bulletList.size() > bulletLimit){
                bulletList.get(nextRemoved).setNew(x, y, right);;
                if(nextRemoved  >= bulletList.size() - 1){
                    nextRemoved = 1;
                } else {
                    nextRemoved++;
                }
            } else {
                bulletList.add(new JoinHeroBullet(this, x, y,right));
            }
            right=!right;
            timePassed += delta;
            lastShot = timePassed;
        }

        else{
            timePassed += delta;
        }


    }

    /**
     * Methode für Online-Schuss, die sofort eine Nachricht zum Server schickt
     * @param x
     * @param y
     * @param delta
     * @param client
     */
    public void multiplayerShoot(float x, float y, float delta, Client client)
    {

        //Nur alle 0.1 Sekunden schießen
        if((timePassed + delta) - lastShot >= 0.1){
            game.playShoot();
            if(bulletList.size() > bulletLimit){
                bulletList.get(nextRemoved).setNew(x, y, right);;
                if(nextRemoved  >= bulletList.size() - 1){
                    nextRemoved = 1;
                } else {
                    nextRemoved++;
                }
            } else {
                bulletList.add(new JoinHeroBullet(this, x, y,right));
            }
            client.sendTCP(new ShootingMessage(x,y));
            right=!right;
            timePassed += delta;
            lastShot = timePassed;
        }

        else{
            timePassed += delta;
        }
    }

    /**
     * Schuss-Methode, ohne Zeitüberprüfung. Dadurch ist garantiert, dass der Join-Hero tatsächlih genau dann beim Server schießt, wenn er es auch beim Client macht.
     * @param x
     * @param y
     * @param delta
     */
    public void forceShoot(float x, float y , float delta)
    {

            game.playShoot();
            if(bulletList.size() > bulletLimit){
                bulletList.get(nextRemoved).setNew(x, y, right);;
                if(nextRemoved  >= bulletList.size() - 1){
                    nextRemoved = 1;
                } else {
                    nextRemoved++;
                }
            } else {
                bulletList.add(new JoinHeroBullet(this, x, y,right));
            }
            right=!right;
            timePassed += delta;
            lastShot = timePassed;

    }

    public void defineBody()
    {
        BodyDef bdef = new BodyDef();
        bdef.fixedRotation=false;
        bdef.position.set(200 / MyGdxGame.PPM, 200 / MyGdxGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);
        b2body.setGravityScale(0);

        fdef = new FixtureDef();
        CircleShape cirlceShape = new CircleShape();
        cirlceShape.setRadius(20 / MyGdxGame.PPM);
        cirlceShape.setPosition(new Vector2(0,40/MyGdxGame.PPM));
        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(5 / MyGdxGame.PPM, 40 / MyGdxGame.PPM);




        fdef.shape = rectShape;
        fdef.density = 1f;
        b2body.createFixture(fdef).setUserData("joinhero");
        fdef.shape= cirlceShape;
        fdef.density=0f;

       // b2body.createFixture(fdef).setUserData("circ");
        b2body.setFixedRotation(false);
        b2body.setTransform(2,2,45);


    }
    public World getWorld()
    {
        return world;
    }
    public Body getBody()
    {
        return  b2body;
    }
    public ArrayList<JoinHeroBullet> getBulletList()
    {
        return bulletList;
    }

    public void setBulletList(ArrayList<JoinHeroBullet> bulletList) {
        this.bulletList = bulletList;
    }
}
