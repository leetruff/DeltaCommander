package enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.MyGdxGame;

public class EnemyBullet extends Sprite{

	Body body;
	int speed;
	EnemySoldier enemy;
	
	int damage;
	boolean hitObject;
	
	public EnemyBullet(EnemySoldier enemy, float mouseX, float mouseY, int size){
		
		super(new Texture(Gdx.files.internal("sprites/red_bullet.png")));
		setBounds(0, 0, 15 / MyGdxGame.PPM, 15 / MyGdxGame.PPM);
		
		
		BodyDef bdef = new BodyDef();
		bdef.position.set(enemy.getBody().getPosition().x, enemy.getBody().getPosition().y + 4 / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = enemy.getWorld().createBody(bdef);
		body.setGravityScale(0);
		body.setUserData(this);
		
		damage = 15;
		hitObject = false;
		
		FixtureDef fdef = new FixtureDef();
		
		//isSensor damit Projektile nicht an Waenden abprallen
		fdef.isSensor = true;
		CircleShape shape = new CircleShape();
		shape.setRadius(size / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("enemybullet");
		
		this.enemy = enemy;
		
		//TODO Fuer Gegner anpassen
		//Trigonometrie Kram, Winkel zum Mauszeiger ausrechnen
		double angle = Math.atan2(mouseX - enemy.getBody().getPosition().x, mouseY - enemy.getBody().getPosition().y);
		speed = 2;

		//System.out.println(mouseX + " " + mouseY  + " " + enemy.b2body.getPosition().x + " " + enemy.b2body.getPosition().y);
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
	}
	
	public EnemyBullet(float xPos, float yPos, float angle, int size, World world){
		
		super(new Texture(Gdx.files.internal("sprites/red_bullet.png")));
		setBounds(0, 0, 26 / MyGdxGame.PPM, 26 / MyGdxGame.PPM);
		
		
		BodyDef bdef = new BodyDef();
		bdef.position.set(xPos, yPos);
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = world.createBody(bdef);
		body.setGravityScale(0);
		body.setUserData(this);
		
		damage = 25;
		hitObject = false;
		
		FixtureDef fdef = new FixtureDef();
		
		//isSensor damit Projektile nicht an Waenden abprallen
		fdef.isSensor = true;
		CircleShape shape = new CircleShape();
		shape.setRadius(size / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("alienbossbullet");
		
		
		//TODO Fuer Gegner anpassen
		//Trigonometrie Kram, Winkel zum Mauszeiger ausrechnen
		speed = 3;

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
	
	
	//Setzt die Kugel neu (um nicht staendig neue Kugeln erzeugen zu muessen)
	public void setNew(float xPos, float yPos, float angle){
		body.setTransform(xPos, yPos, (int) (angle + 0.5));
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
		
		//TODO machen keinen dmg wenn map kollision
	}
}
