package network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.MyGdxGame;


/**
 * Created by hermann on 18.06.16.
 */
public class JoinHeroBullet extends Sprite {

    Body body;
    int speed;
    JoinHero joinHero;
    int damage;
    boolean hitObject;

    /**
     * Schüsse des JoinPlayers im Multiplayer-Modus
     * @param joinHero JoinHero Instanz
     * @param mouseX X-Koordinate der Berührung
     * @param mouseY Y-Koordinate der Berührung
     * @param right Boolean, um abwechselnd aus den beiden Läufen des JoinHeros zu feuern, true für rechten Lauf
     */
    public JoinHeroBullet(JoinHero joinHero, float mouseX, float mouseY,boolean right){

        super(new Texture(Gdx.files.internal("sprites/joinbullet.png")));
        setBounds(0, 0, 15 / MyGdxGame.PPM, 15 / MyGdxGame.PPM);


        BodyDef bdef = new BodyDef();
        bdef.position.set((right)?joinHero.getBody().getPosition().x+joinHero.getWidth()/2:joinHero.getBody().getPosition().x-joinHero.getWidth()/2, joinHero.getBody().getPosition().y-joinHero.getHeight()/2);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = joinHero.getWorld().createBody(bdef);
        body.setGravityScale(0);
        body.setUserData(this);


        FixtureDef fdef = new FixtureDef();

        //isSensor damit Projektile nicht an Waenden abprallen
        fdef.isSensor = true;
        CircleShape shape = new CircleShape();
        shape.setRadius(9 / MyGdxGame.PPM);


        fdef.shape = shape;
        body.createFixture(fdef).setUserData("joinherobullet");

        double angle = Math.atan2(mouseX - joinHero.getBody().getPosition().x, mouseY - joinHero.getBody().getPosition().y);
        speed = 3;
        damage=10;
        hitObject=false;

        body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));

        this.joinHero=joinHero;
    }



    //Setzt die Kugel neu (um nicht staendig neue Kugeln erzeugen zu muessen)
    public void setNew(float mouseX, float mouseY, boolean right){

        double angle = Math.atan2(mouseX - joinHero.getX(), mouseY - joinHero.getY());
        body.setTransform((right)?joinHero.getBody().getPosition().x+joinHero.getWidth()/2:joinHero.getBody().getPosition().x-joinHero.getWidth()/2, joinHero.getBody().getPosition().y-joinHero.getHeight()/2, (int) (angle + 0.5));
        body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
        hitObject=false;
    }

    public void update(float delta) {
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
    }

    public int getDamage() {
        return damage;
    }

    public void setHitObject(boolean hitObject) {
        this.hitObject = hitObject;
    }

    public boolean isHitObject() {
        return hitObject;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}


