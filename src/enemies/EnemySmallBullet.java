package enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.MyGdxGame;

public class EnemySmallBullet extends Sprite{

	Body body;
	int speed;
	EnemyTower enemy;
	
	int damage;
	boolean hitObject;
	
	public EnemySmallBullet(EnemyTower enemy, float mouseX, float mouseY, int size){
		
		super(new Texture(Gdx.files.internal("sprites/smallbullet.png")));
		setBounds(0, 0, 6 / MyGdxGame.PPM, 6 / MyGdxGame.PPM);
		
		
		BodyDef bdef = new BodyDef();
		bdef.position.set(enemy.getBody().getPosition().x, enemy.getBody().getPosition().y + 4 / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = enemy.getWorld().createBody(bdef);
		body.setGravityScale(0);
		body.setUserData(this);
		
		damage = 10;
		hitObject = false;
		
		FixtureDef fdef = new FixtureDef();
		
		//isSensor damit Projektile nicht an Waenden abprallen
		fdef.isSensor = true;
		CircleShape shape = new CircleShape();
		shape.setRadius(size / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("enemysmallbullet");
		
		this.enemy = enemy;
		
		//TODO Fuer Gegner anpassen
		//Trigonometrie Kram, Winkel zum Mauszeiger ausrechnen
		double angle = Math.atan2(mouseX - enemy.getBody().getPosition().x, mouseY - enemy.getBody().getPosition().y);
		speed = 2;

		//System.out.println(mouseX + " " + mouseY  + " " + enemy.b2body.getPosition().x + " " + enemy.b2body.getPosition().y);
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
	}
	
	
	
	//Setzt die Kugel neu (um nicht staendig neue Kugeln erzeugen zu muessen)
	public void setNew(float mouseX, float mouseY){
		double angle = Math.atan2(mouseX - enemy.b2body.getPosition().x, mouseY - enemy.b2body.getPosition().y);
		body.setTransform(enemy.b2body.getPosition().x, enemy.b2body.getPosition().y, (int) (angle + 0.5));
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
		
		hitObject = false;
	}

	public void update(float delta) {
		setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
	
	public int getDamage() {
		return damage;
	}

	public boolean isHitObject() {
		return hitObject;
	}

	public void setHitObject(boolean hitObject) {
		this.hitObject = hitObject;
	}
}
